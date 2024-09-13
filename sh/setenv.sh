#!/bin/sh

#################################################
#                                               #
#        Configuration for Playce RoRo          #
#                                               #
#################################################

# Set JAVA_HOME when default java version is not JDK 11
#JAVA_HOME=/usr/lib/jvm/java-11

# Port offset for HTTP
PORT_OFFSET=2

# Working directory for RoRo (Inventory, Assessment, Migration and etc.)
WORKING_DIR=/roro

# DB Connection URL
DB_URL=jdbc:mariadb://localhost:3306/rorodb

# DB Username
DB_USERNAME=playce

# DB Password
DB_PASSWORD=playce

if [ e$WORKING_DIR = "e" ] ; then
    echo "[Error] WORKING_DIR must be set."
    exit;
fi

if [ e$DB_URL = "e" ] ; then
    echo "[Error] DB_URL must be set."
    exit;
fi

if [ e$DB_USERNAME = "e" ] ; then
    echo "[Error] DB_USERNAME must be set."
    exit;
fi

if [ e$DB_PASSWORD = "e" ] ; then
    echo "[Error] DB_PASSWORD must be set."
    exit;
fi

# Log file path
JAVA_OPTS="$JAVA_OPTS -DLOG_PATH=$CATALINA_HOME/logs/"
JAVA_OPTS="$JAVA_OPTS -Dlogging.file.name=$CATALINA_HOME/logs/playce-roro.log"

# File encoding
JAVA_OPTS="$JAVA_OPTS -Dfile.encoding=UTF-8 -Dfile.client.encoding=UTF-8"

# Additional config
JAVA_OPTS="$JAVA_OPTS -Xms2048m -Xmx2048m -XX:MetaspaceSize=256m -XX:MaxMetaspaceSize=256m"
JAVA_OPTS="$JAVA_OPTS -XX:+UseG1GC"
JAVA_OPTS="$JAVA_OPTS -XX:+UseLargePagesInMetaspace"
JAVA_OPTS="$JAVA_OPTS -XX:+ExplicitGCInvokesConcurrent"
JAVA_OPTS="$JAVA_OPTS -XX:+DisableExplicitGC"
JAVA_OPTS="$JAVA_OPTS -XX:ReservedCodeCacheSize=512m"
JAVA_OPTS="$JAVA_OPTS -XX:-UseCodeCacheFlushing"
JAVA_OPTS="$JAVA_OPTS -Djava.security.egd=file:/dev/urandom"
JAVA_OPTS="$JAVA_OPTS -Djava.net.preferIPv4Stack=true"
JAVA_OPTS="$JAVA_OPTS -Djava.security.properties=$CATALINA_HOME/conf/security/java.security"

JAVA_OPTS="$JAVA_OPTS -Dhttp.port=$(expr 8080 + $PORT_OFFSET)"
JAVA_OPTS="$JAVA_OPTS -Dajp.port=$(expr 8009 + $PORT_OFFSET)"
JAVA_OPTS="$JAVA_OPTS -Dssl.port=$(expr 8443 + $PORT_OFFSET)"
JAVA_OPTS="$JAVA_OPTS -Dshutdown.port=$(expr 8005 + $PORT_OFFSET)"

## Application config
# Enable scheduler for prerequisite, assessment and migration
JAVA_OPTS="$JAVA_OPTS -Denable.prerequisite.schedule=true"
JAVA_OPTS="$JAVA_OPTS -Denable.assessment.schedule=true"
JAVA_OPTS="$JAVA_OPTS -Denable.migration.schedule=true"

# Set schedule for monitoring and scheduled scan
# Server monitoring and scheduled scan are controlled in the Settings menu of the RoRo Console.
JAVA_OPTS="$JAVA_OPTS -Dscheduler.schedule.monitoring.cron='0 0 0/3 * * ?'"
JAVA_OPTS="$JAVA_OPTS -Dscheduler.schedule.scheduled-scan.cron='0 0 0 * * ?'"

JAVA_OPTS="$JAVA_OPTS -Dspring.datasource.url='$DB_URL'"
JAVA_OPTS="$JAVA_OPTS -Dspring.datasource.username='$DB_USERNAME'"
JAVA_OPTS="$JAVA_OPTS -Dspring.datasource.password='$DB_PASSWORD'"

# Log Viewer Authentication
#JAVA_OPTS="$JAVA_OPTS -Droro.log.viewer.username='admin'"
#JAVA_OPTS="$JAVA_OPTS -Droro.log.viewer.password='admin'"

# RoRo working directory
# Make sure roro.ssh.user-name can access the WORKING_DIR
JAVA_OPTS="$JAVA_OPTS -Droro.working.dir-path=$WORKING_DIR"

