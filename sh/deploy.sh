#!/bin/bash

PRIV=/var/jenkins_home/.ssh/id_rsa
APP_NAME=roro
SOURCE='roro-main/roro-web/target/'$APP_NAME'-web.war'
TARGET_DIR=/opt/playce/playce-$APP_NAME

echo 'copy config'
scp -i $PRIV sh/setenv.sh sh/kill.sh sh/tail.sh sh/create_roro_db.sql osc@$SERVER:$TARGET_DIR/bin/
scp -i $PRIV sh/server.xml osc@$SERVER:$TARGET_DIR/conf/
ssh -i $PRIV osc@$SERVER 'mkdir -p '$TARGET_DIR/conf/security
scp -i $PRIV sh/java.security osc@$SERVER:$TARGET_DIR/conf/security/
echo 'copy db_patch'
ssh -i $PRIV osc@$SERVER 'mkdir -p '$TARGET_DIR/db_patch
scp -i $PRIV sh/db_patch/*.sql osc@$SERVER:$TARGET_DIR/db_patch/
echo 'copy app'
scp -i $PRIV $SOURCE 'osc@'$SERVER:$TARGET_DIR'/webapps/'$APP_NAME'.war'
echo 'unzip app'
ssh -i $PRIV osc@$SERVER 'cd '$TARGET_DIR'/webapps && rm -rf '$APP_NAME' && unzip -d '$APP_NAME' '$APP_NAME'.war && rm -f '$APP_NAME'.war'
