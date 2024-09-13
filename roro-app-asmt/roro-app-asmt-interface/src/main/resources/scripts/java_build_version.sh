#!/bin/env bash
if [[ $1 == *MANIFEST.MF ]]
then
  cat $1 | grep -i build | grep -i jdk | awk -F':' '{print$2}' | tr -d ' ' | awk -F'.0' '{print $1}'
else
  JAVAP_PATH=$(which javap 2>/dev/null)

  if [ "x$JAVAP_PATH" != "x" ]; then
    VERSION=`$JAVAP_PATH -v $1 | grep 'major version' | head -n 1 | awk -F': ' '{print $2}'`
    case $VERSION in
      "45") echo "1.1"
      ;;
      "46") echo "1.2"
      ;;
      "47") echo "1.3"
      ;;
      "48") echo "1.4"
      ;;
      "49") echo "1.5"
      ;;
      "50") echo "1.6"
      ;;
      "51") echo "1.7"
      ;;
      "52") echo "1.8"
      ;;
      "53") echo "9"
      ;;
      "54") echo "10"
      ;;
      "55") echo "11"
      ;;
      "56") echo "12"
      ;;
      "57") echo "13"
      ;;
      "58") echo "14"
      ;;
      "59") echo "15"
      ;;
      "60") echo "16"
      ;;
      "61") echo "17"
      ;;
      "62") echo "18"
      ;;
      "63") echo "19"
      ;;
    esac
  else
    echo "Error: javap command not found in PATH"
    exit 1
  fi
fi