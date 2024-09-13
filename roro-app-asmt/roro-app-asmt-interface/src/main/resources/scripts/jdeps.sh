#!/bin/bash

# Make sure prerequisite environment variables are set
if [ -z "$JAVA_HOME" ]; then
    if $darwin; then
        if [ -x '/usr/libexec/java_home' ] ; then
            export JAVA_HOME=`/usr/libexec/java_home`
        elif [ -d "/System/Library/Frameworks/JavaVM.framework/Versions/CurrentJDK/Home" ]; then
            export JAVA_HOME="/System/Library/Frameworks/JavaVM.framework/Versions/CurrentJDK/Home"
        fi
    else
        JAVA_PATH=`which java 2> /dev/null`
        if [ "x$JAVA_PATH" != "x" ]; then
            JAVA_PATH=`dirname "$JAVA_PATH" 2> /dev/null`
            JAVA_HOME=`dirname "$JAVA_PATH" 2> /dev/null`
        fi
    fi

    if [ "x$JAVA_HOME" = "x" ]; then
        if [ -x /usr/bin/java ]; then
            JAVA_HOME=/usr
        elif [ -x /bin/java ]; then
            JAVA_HOME=/
        fi
    fi
fi

if [ -z "$JAVA_HOME" ] || [ ! -x "$JAVA_HOME"/bin/jdeps ]; then
    echo "[ERROR] jdeps does not exist."
    exit
fi

if [ e$1 = "e--version" ]; then
    $JAVA_HOME/bin/jdeps --version
else
    $JAVA_HOME/bin/jdeps --jdk-internals $1
fi