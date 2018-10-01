# autoDeployer

GitHub Webhook Client - Spring Boot



Prerequisite
Java Runtime and JAVA_HOME is properly set.
Maven is installed on your box.
Build, Deploy and Run
On your linux box, clone the github project and execute below commands from project directory.

Note the GHSecretKey generated. Exact same key should be used on Github.com while configuring this webhook.

mvn clean package

#Copy run_app.sh and make sure its executable.
sudo cp run_app.sh /srv/autoDeployerGithubWebHook/ && sudo chmod +x /srv/autoDeployerGithubWebHook

#To enable webhook start on startup open cron tab
crontab -e
#Then append below line to crontab file and save
@reboot /srv/autoDeployerGithubWebHook/run_app.sh

#If you can't reboot system now, then just run it manually first tie -
sh /srv/autoDeployerGithubWebHook/run_app.sh

#To verify it started, check java process
ps -ef | grep autoDeployerGithubWebHook

Once webhook runs, it will be accessible over http(/s)://{your ip/domain}:9050/push

Configuration on Github.com
Go to your repository -> Settings -> Webhooks . Add a new webhook with your address there and use the exact same Secret Key generated in GHSecretKey step during deploying.


