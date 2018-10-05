#!/bin/bash

#declaring variables

appName="autoDeployerGithubWebHook"
dir=""
appDir="$dir/autoDeployer"
logDir="$dir/logs"


#check existing folder
if [ ! -d "$logDir" ]; then
  mkdir $logDir
fi

if [ ! -d "$logDir/$appName" ]; then
  mkdir $logDir/$appName
fi

cd $appDir

export GHSecretKey=
#overwrite output to a file log
cat <<EOF > $appDir/run_app_log.txt

`git pull`
`mvn clean package`
`nohup java -jar ../target/$appName.jar > /$logDir/$appName/log.txt 2> /$logDir/$appName/errors.txt &`

EOF
