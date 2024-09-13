#!/bin/bash
CNT=$#
if [ $CNT = 3 ] ; then
    ps -ef | grep python | grep -w "$1" | grep -w "$2" | grep "/application/$3" | grep -v grep | grep -v app_scan_cancel.sh | awk {'print "kill -9 " $2'} | sh -x
    ps -ef | grep tar | grep "/application/$3" | grep -v grep | grep -v app_scan_cancel.sh | awk {'print "kill -9 " $2'} | sh -x
    ps -ef | grep sh | grep "/application/$3" | grep -v grep | grep -v app_scan_cancel.sh | awk {'print "kill -9 " $2'} | sh -x
else
    echo "[Usage] => ./app_scan_cancel.sh {ip_address} {port} {application_assessment_id}"
fi