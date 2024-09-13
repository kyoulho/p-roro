#!/bin/sh

function help
{
    echo "Usage: $0 <interval_seconds> <hour> <path> <isSudoer> [type]"
    echo "TYPE is one of [cpu, memory, disk, process, network]"
}

function cpu
{
    if [ e${GV_IS_SUDOER} = "etrue" ] ; then
        echo ${GV_DATE},$(sudo sar 1 2 | grep -i average | awk '{print $2+$3}') >> ${GV_STAT_FILE_PATH}/cpu.stat
    else
        echo ${GV_DATE},$(sar 1 2 | grep -i average | awk '{print $2+$3}') >> ${GV_STAT_FILE_PATH}/cpu.stat
    fi
}

function memory
{
    if [ e${GV_IS_SUDOER} = "etrue" ] ; then
        echo ${GV_DATE},$(sudo svmon -i 1 1 | grep -i memory | awk '{print $2","$3","$4"," ($3/$2)*100}') >> ${GV_STAT_FILE_PATH}/memory.stat
    else
        echo ${GV_DATE},$(svmon -i 1 1 | grep -i memory | awk '{print $2","$3","$4"," ($3/$2)*100}') >> ${GV_STAT_FILE_PATH}/memory.stat
    fi
}

function disk
{
    SIZE=$(df -Pk | grep -v proc | wc -l)
    i=2
    while  [[ $i -le $SIZE ]]
    do
        RESULT_LINE=$(df -Pk | grep -v proc | sed -n ${i}p)
        TEMP_STR=${GV_DATE}

        #device,partition,total,used,available,utilization
        TEMP_STR=${TEMP_STR},$(echo ${RESULT_LINE} | awk "{print \$1}")
        TEMP_STR=${TEMP_STR},$(echo ${RESULT_LINE} | awk '{print "lsfs "$1}' | sh | grep -vi name | awk '{print $4}')
        TEMP_STR=${TEMP_STR},$(echo ${RESULT_LINE} | awk "{print \$6}")
        TEMP_STR=${TEMP_STR},$(echo ${RESULT_LINE} | awk "{print \$2}")
        TEMP_STR=${TEMP_STR},$(echo ${RESULT_LINE} | awk "{print \$3}")
        TEMP_STR=${TEMP_STR},$(echo ${RESULT_LINE} | awk "{print \$4}")
        TEMP_STR=${TEMP_STR},$(echo ${RESULT_LINE} | awk "{print \$5}")
        echo $TEMP_STR >> ${GV_STAT_FILE_PATH}/disk.stat
        let i=i+1
    done
}

function process
{
    SIZE=$(ps -ef -o pid,comm,user,args | wc -l)
    i=2
    while  [[ $i -le $SIZE ]]
    do
        RESULT_LINE=$(ps -ef -o pid,comm,user,args | sed -n ${i}p)
        TEMP_STR=${GV_DATE}

        for j in 1 2 3
        do
            TEMP_STR=${TEMP_STR},$(echo ${RESULT_LINE} | awk "{print \$${j}}")
        done

        TEMP_STR=${TEMP_STR},\"$(echo ${RESULT_LINE} | awk '{$1=$2=$3=""; print $0}')\"
        TEMP_STR=$(echo ${TEMP_STR} | sed -e 's/," /,"/')

        echo $TEMP_STR >> ${GV_STAT_FILE_PATH}/process.stat
        let i=i+1
    done
}

function network
{
    SIZE=$(netstat -anA | egrep -i "tcp|udp" |  wc -l)
    i=1
    while  [[ $i -le $SIZE ]]
    do
        RESULT_LINE=$(netstat -anA | egrep -i "tcp|udp" | sed 's/\*\./0.0.0.0:/g' | sed -n ${i}p)
        TEMP_STR=${GV_DATE}
        PROTOCOL_TYPE=$(echo ${RESULT_LINE} | awk '{print $2}')

        #protocol
        TEMP_STR=${TEMP_STR},${PROTOCOL_TYPE}

        #sate
        case ${PROTOCOL_TYPE} in
            "udp" | "udp4" | "udp6")
                #udp state is null
                TEMP_STR=${TEMP_STR},
            ;;
            "tcp" | "tcp4" | "tcp6")
                TEMP_STR=${TEMP_STR},$(echo ${RESULT_LINE} | awk '{print $7}')
            ;;
        esac

        #localAddress,localPort
        LOCAL_IP_PORT=$(echo ${RESULT_LINE} | awk '{print $5}' | sed -e 's/\*//')

        if [ $(echo ${LOCAL_IP_PORT} | tr -cd "." | wc -m) -gt 3 ]
        then
            TEMP_STR=${TEMP_STR},$(echo ${LOCAL_IP_PORT} | awk -F. '{print $1"."$2"."$3"."$4}')
            TEMP_STR=${TEMP_STR},$(echo ${LOCAL_IP_PORT} | awk -F. '{print $NF}')
        else
            if [ $(echo ${LOCAL_IP_PORT} | tr -cd ":" | wc -m) -gt 1 ]
            then
                TEMP_STR=${TEMP_STR},$(echo ${LOCAL_IP_PORT} | awk -F. '{print $1}')
                TEMP_STR=${TEMP_STR},$(echo ${LOCAL_IP_PORT} | awk -F. '{print $2}')
            else
                TEMP_STR=${TEMP_STR},$(echo ${LOCAL_IP_PORT} | awk -F: '{print $1}')
                TEMP_STR=${TEMP_STR},$(echo ${LOCAL_IP_PORT} | awk -F: '{print $2}')
            fi
        fi

        #foreignAddress,foreignPort
        FOREIGN_IP_PORT=$(echo ${RESULT_LINE} | awk '{print $6}' | sed -e 's/\*//')

        if [ $(echo ${FOREIGN_IP_PORT} | tr -cd "." | wc -m) -gt 3 ]
        then
            TEMP_STR=${TEMP_STR},$(echo ${FOREIGN_IP_PORT} | awk -F. '{print $1"."$2"."$3"."$4}')
            TEMP_STR=${TEMP_STR},$(echo ${FOREIGN_IP_PORT} | awk -F. '{print $NF}')
        else
            if [ $(echo ${FOREIGN_IP_PORT} | tr -cd ":" | wc -m) -gt 1 ]
            then
                TEMP_STR=${TEMP_STR},$(echo ${FOREIGN_IP_PORT} | awk -F. '{print $1}')
                TEMP_STR=${TEMP_STR},$(echo ${FOREIGN_IP_PORT} | awk -F. '{print $2}')
            else
                TEMP_STR=${TEMP_STR},$(echo ${FOREIGN_IP_PORT} | awk -F: '{print $1}')
                TEMP_STR=${TEMP_STR},$(echo ${FOREIGN_IP_PORT} | awk -F: '{print $2}')
            fi
        fi


        #PCB Address (Instead of PID)
        TEMP_STR=${TEMP_STR},$(echo ${RESULT_LINE} | awk '{print $1}')

        echo $TEMP_STR >> ${GV_STAT_FILE_PATH}/network.stat
        let i=i+1
    done
}

function now
{
    perl -e 'use Time::Local; print time()'
}

function epoch2fmt
{
    perl -e 'use POSIX 'strftime'; print strftime("%Y%m%d%H%M%S", gmtime('$1')), "\n";'
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

GV_END_TIME=$(expr $(now) + $(expr ${GV_HOUR} \* 3600))

GV_CURRENT_TIME=$(now)
#GV_DATE=$(fmt ${GV_CURRENT_TIME})
GV_DATE=$(date "+%Y%m%d%H%M%S")

while [ ${GV_CURRENT_TIME} -lt ${GV_END_TIME} ]
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
    GV_CURRENT_TIME=$(now)
    GV_DATE=$(date "+%Y%m%d%H%M%S")
done