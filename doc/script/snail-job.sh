#!/bin/sh
# ./snail-job.sh start|空 启动 stop 停止 restart 重启 status 状态
AppName=snail-job-server-exec.jar

# JVM参数
JVM_OPTS="-Dname=$AppName  -Duser.timezone=Asia/Shanghai -XX:+HeapDumpOnOutOfMemoryError -XX:+UseZGC"
APP_HOME=`pwd`

# 检查传入的操作名参数，如果为空则默认设置为 "start"
if [ "$1" = "" ]; then
    echo -e "\033[0;34m 未输入操作名，默认操作为: start \033[0m"
    ACTION="start"
else
    ACTION=$1
fi

if [ "$AppName" = "" ]; then
    echo -e "\033[0;31m 未输入应用名 \033[0m"
    exit 1
fi

function start() {
    PID=`ps -ef |grep java|grep $AppName|grep -v grep|awk '{print $2}'`

    if [ x"$PID" != x"" ]; then
        echo "$AppName is running..."
    else
        nohup java $JVM_OPTS -jar $AppName > /dev/null 2>&1 &
        echo "Start $AppName success..."
    fi
}

function stop() {
    echo "Stop $AppName"

    PID=""
    query(){
        PID=`ps -ef |grep java|grep $AppName|grep -v grep|awk '{print $2}'`
    }

    query
    if [ x"$PID" != x"" ]; then
        kill -TERM $PID
        echo "$AppName (pid:$PID) exiting..."
        while [ x"$PID" != x"" ]
        do
            sleep 1
            query
        done
        echo "$AppName exited."
    else
        echo "$AppName already stopped."
    fi
}

function restart() {
    stop
    sleep 2
    start
}

function status() {
    PID=`ps -ef |grep java|grep $AppName|grep -v grep|wc -l`
    if [ $PID != 0 ];then
        echo "$AppName is running..."
    else
        echo "$AppName is not running..."
    fi
}

case $ACTION in
    start)
    start;;
    stop)
    stop;;
    restart)
    restart;;
    status)
    status;;
    *)
    echo -e "\033[0;31m 无效的操作名 \033[0m  \033[0;34m {start|stop|restart|status} \033[0m";;
esac