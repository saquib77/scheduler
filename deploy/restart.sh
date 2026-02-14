#!/bin/bash

time=$(date +"%Y-%m-%d")
rport=$1
jfile=$2
wdir=$3
logfile=$4
#status=$(lsof -t -i:$rport)
PID=$(netstat -nlp | grep $rport | awk -F "[ /]+" '{print $7}')
START=$(nohup java -jar $jfile > $logfile 2>&1 &)

RestartJar() {
if [ "$PID" ]; then
  kill -9 $PID
  $START
else
  $START
fi
}

RestartJar
