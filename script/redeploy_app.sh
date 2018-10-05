#!/bin/bash

appName="autoDeployerGithubWebHook"
appPid=`jps | grep $appName.jar | awk '{print $1}'`
dir=""
appDir="$dir/autoDeployer"
logDir="$dir/logs"

if [ -z "$appPid" ]; then
    echo "app is not running"
else
    echo "app pid is $appPid"
    kill $appPid
    echo "killed pid $appPid"
fi

mvn clean package
nohup java -jar ../target/$appName.jar > /$logDir/$appName/log.txt 2> /$logDir/$appName/errors.txt &