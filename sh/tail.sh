#!/bin/sh

BASE_DIR="$(cd "$(dirname "$0")"; pwd)";
tail -50f $BASE_DIR/../logs/catalina.out