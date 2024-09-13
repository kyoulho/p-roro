#!/bin/bash
CNT=$#
if [ $CNT = 1 ] ; then
    ps -ef | grep -w "$1" | grep -v grep | grep -v cancel_migration.sh | awk {'print "kill -9 " $2'} | sh -x
elif [ $CNT = 2 ] ; then
    ps -ef | grep -w "$1" | grep -w "$2" | grep -v grep | grep -v cancel_migration.sh | awk {'print "kill -9 " $2'} | sh -x
    ps -ef | grep python | grep -w "$2" | grep -v grep | grep -v cancel_migration.sh | awk {'print "kill -9 " $2'} | sh -x
    ps -ef | grep tar | grep -w "$2" | grep -v grep | grep -v cancel_migration.sh | awk {'print "kill -9 " $2'} | sh -x
    ps -ef | grep sshpass | grep -w "$2" | grep -v grep | grep -v cancel_migration.sh | awk {'print "kill -9 " $2'} | sh -x
else
    echo "[Usage] => ./cancel_migration.sh {work_dir} {ip_address}"
fi