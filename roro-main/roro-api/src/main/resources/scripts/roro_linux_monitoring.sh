#!/bin/bash

function help()
{
    echo "Usage: $0 <interval_seconds> <hour> <path> [type]"
    echo "TYPE is one of [cpu, memory, disk, process, network]"
}

function cpu()
{
    echo $(date --date="@${GV_DATE}" "+%Y%m%d%H%M%S"),$(top -b -n1 | head -5 | grep -Po '[0-9.]+.id' | sed -e 's/%/ /' | awk '{print 100-$1}') >> ${GV_STAT_FILE_PATH}/cpu.stat
}

function memory()
{
    TEMP_STR=$(date --date="@${GV_DATE}" "+%Y%m%d%H%M%S")

    RESULT_LINE=$(free -k | grep Mem:)

    #total,used,available,utilization
    for i in 2 3 7
    do
        TEMP_STR=${TEMP_STR},$(echo ${RESULT_LINE} | awk "{print \$${i}}")
    done

    TEMP_STR=${TEMP_STR},$(echo ${RESULT_LINE} | awk '{printf "%.2f", 100 - ($7/$2*100)}')

    echo $TEMP_STR >> ${GV_STAT_FILE_PATH}/memory.stat
}

function disk()
{
    SIZE=$(df -kTP | sed 1d | grep -v -E "none|udev|sysfs|cgroup|pstore|securityfs|rootfs|nfsd|configfs|rpc_pipefs|selinuxfs|autofs|debugfs|devtmpfs|tmpfs|drivefs|devfs|devpts|binfmt_misc|systemd|gvfsd-fuse" | wc -l)

    for (( i=1; i<=${SIZE}; i++ ))
    do
        RESULT_LINE=$(df -kTP | sed 1d | grep -v -E "none|udev|sysfs|cgroup|pstore|securityfs|rootfs|nfsd|configfs|rpc_pipefs|selinuxfs|autofs|debugfs|devtmpfs|tmpfs|drivefs|devfs|devpts|binfmt_misc|systemd|gvfsd-fuse" | sed -n ${i}p)
        TEMP_STR=$(date --date="@${GV_DATE}" "+%Y%m%d%H%M%S")

        #device,type,partition,total,used,available,utilization
        for j in 1 2 7 3 4 5 6
        do
            TEMP_STR=${TEMP_STR},$(echo ${RESULT_LINE} | awk "{print \$${j}}")
        done

        echo $TEMP_STR >> ${GV_STAT_FILE_PATH}/disk.stat
    done
}

function process()
{
    # delete kernel thread
    SIZE=$(ps -ef | sed -e "1d" -e "/]$/d" | awk '{print $8}' | wc -l)

    for (( i=1; i<${SIZE}; i++ ))
    do
        # delete kernel thread
        RESULT_LINE=$(ps -ef | sed -e "1d" -e "/]$/d" | sed -n ${i}p)
        TEMP_STR=$(date --date="@${GV_DATE}" "+%Y%m%d%H%M%S")

        #pid,name,username,command
        for j in 2 8 1
        do
            TEMP_STR=${TEMP_STR},$(echo ${RESULT_LINE} | awk "{print \$${j}}")
        done

        TEMP_STR=${TEMP_STR},\"$(echo ${RESULT_LINE} | awk '{$1=$2=$3=$4=$5=$6=$7=""; print $0}')\"
        TEMP_STR=$(echo ${TEMP_STR} | sed -e 's/," /,"/')

        echo $TEMP_STR >> ${GV_STAT_FILE_PATH}/process.stat
    done
}

function network()
{
    if [ e${GV_IS_SUDOER} = "etrue" ]
    then
        SIZE=$(sudo netstat -antup | sed -e '1,2d' | wc -l)
    else
        SIZE=$(netstat -antup | sed -e '1,2d' | wc -l)
    fi

    for (( i=1; i<=${SIZE}; i++ ))
    do
        if [ e${GV_IS_SUDOER} = "etrue" ]
        then
            RESULT_LINE=$(sudo netstat -antup | sed -e '1,2d' | sed -n ${i}p)
        else
            RESULT_LINE=$(netstat -antup | sed -e '1,2d' | sed -n ${i}p)
        fi
        TEMP_DATE=$(date --date="@${GV_DATE}" "+%Y%m%d%H%M%S")
        TEMP_STR=${TEMP_DATE}
        PROTOCOL_TYPE=$(echo ${RESULT_LINE} | awk '{print $1}')

        if [ $(echo ${RESULT_LINE} | tr -cd ":" | wc -m) -gt 2 ]
        then
            if [ $(echo ${PROTOCOL_TYPE} | grep 6 | wc -l) -eq 0 ]
            then
                PROTOCOL_TYPE=${PROTOCOL_TYPE}6
            fi
        fi

        #protocol
        TEMP_STR=${TEMP_STR},${PROTOCOL_TYPE}

        #state
        case ${PROTOCOL_TYPE} in
            "udp" | "udp6")
                #udp state is null
                TEMP_STR=${TEMP_STR},
            ;;
            "tcp" | "tcp6")
                TEMP_STR=${TEMP_STR},$(echo ${RESULT_LINE} | awk '{print $6}')
            ;;
        esac

        #protocol,state,localAddress,localPort,foreignAddress,foreignPort,pid
        case ${PROTOCOL_TYPE} in
            "tcp" | "udp")
                TEMP_STR=${TEMP_STR},$(echo ${RESULT_LINE} | awk '{print $4}' | awk -F: '{print $1}')
                TEMP_STR=${TEMP_STR},$(echo ${RESULT_LINE} | awk '{print $4}' | awk -F: '{print $2}')
                TEMP_STR=${TEMP_STR},$(echo ${RESULT_LINE} | awk '{print $5}' | awk -F: '{print $1}')
                TEMP_STR=${TEMP_STR},$(echo ${RESULT_LINE} | awk '{print $5}' | awk -F: '{print $2}')
            ;;
            "tcp6" | "udp6")
                #local address
                PORT=$(echo ${RESULT_LINE} | awk '{print $4}' | awk -F: '{print $NF}')
                TEMP_STR=${TEMP_STR},$(echo ${RESULT_LINE} | awk '{print $4}' | sed -e "s/:${PORT}$//")
                TEMP_STR=${TEMP_STR},${PORT}

                #foreign address
                PORT=$(echo ${RESULT_LINE} | awk '{print $5}' | awk -F: '{print $NF}')
                TEMP_STR=${TEMP_STR},$(echo ${RESULT_LINE} | awk '{print $5}' | sed -e "s/:${PORT}$//")
                TEMP_STR=${TEMP_STR},${PORT}
            ;;
        esac

        #pid
        case ${PROTOCOL_TYPE} in
            "udp" | "udp6")
                TEMP_STR=${TEMP_STR},$(echo ${RESULT_LINE} | awk '{print $6}' | awk -F/ '{print $1}')
            ;;
            "tcp" | "tcp6")
                TEMP_STR=${TEMP_STR},$(echo ${RESULT_LINE} | awk '{print $7}' | awk -F/ '{print $1}')
            ;;
        esac

        # replace ::*
        TEMP_STR=$(echo ${TEMP_STR} | sed -e 's/:\*,/,/')

        # if result is '<date>,' as changing size, exit to loop
        if [ ${TEMP_STR} = "${TEMP_DATE}," ]
        then
            TEMP_STR=${TEMP_DATE}",,,,,,,"
        fi

        echo $TEMP_STR >> ${GV_STAT_FILE_PATH}/network.stat
    done
}


# ==== main ====

if [[ $# -lt 3 ]]
then
    help
    exit 0
fi

GV_INTERVAL_SEC=$1
GV_HOUR=$2
GV_STAT_FILE_PATH=$3
GV_IS_SUDOER=$4
GV_TYPE=$5

GV_THRESHOLD_DATE_AGO=$(date -d '+'${GV_HOUR}' hour' +%s)

GV_DATE=$(date +%s)

while [ ${GV_DATE} -lt ${GV_THRESHOLD_DATE_AGO} ]
do
    case ${GV_TYPE} in
      "cpu") cpu;;
      "memory") memory;;
      "disk") disk;;
      "process") process;;
      "network") network;;
      "")
        cpu
        memory
        disk
        #process
        network
      ;;
    esac

    sleep ${GV_INTERVAL_SEC}
    GV_DATE=$(date +%s)
done