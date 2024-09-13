#!/bin/sh

PID=`ps -ef | grep java | grep tomcat | grep -E "playce-roro($|\s)" | awk '{print $2}'`

if [ e$PID == "e" ] ; then
    logger -s "playce-roro is not running."
    exit;
fi

ps -ef | grep java | grep tomcat | grep -E "playce-roro($|\s)" | awk {'print "kill -9 " $2'} | sh -x

ps -ef | grep migration_file_downloader.py | grep -v grep | awk {'print "kill -9 " $2'} | sh -x
ps -ef | grep application_file_download.py | grep -v grep | awk {'print "kill -9 " $2'} | sh -x
ps -ef | grep ssh | grep tar | grep StrictHostKeyChecking | grep -v grep | awk {'print "kill -9 " $2'} | sh -x
