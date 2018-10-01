set -a
. /etc/default/autoDeployerGithubWebHook

nohup java -jar /srv/autoDeployerGithubWebHook/autoDeployerGithubWebHook.jar > /srv/autoDeployerGithubWebHook/log.txt 2> /srv/autoDeployerGithubWebHook/errors.txt < /dev/null & PID=$!
echo $PID > pid.txt