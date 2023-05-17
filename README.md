# The RSD and OAI-PMH
We will use the [jOAI project](https://github.com/NCAR/joai-project) as an OAI-PMH server that [OpenAIRE](https://explore.openaire.eu/) can scrape. 
We need to provide XML files in the `oai_datacite` metadata format in order for jOAI to work.
We've developed a small Java program that scrapes the RSD API for releases of software, scrapes the [DataCite OAI server](https://oai.datacite.org/oai/) for its metadata and saves the metadata as XML files.

## Install script
The following script will download and install some of the dependencies.
Note that some manual configuration afterwards is necessary.

```shell
#!/bin/bash
# install Java, Maven and unzip
sudo apt update
sudo apt install openjdk-17-jdk maven unzip

# download and unzip Tomcat
curl --location --output apache-tomcat-7.0.109.tar.gz https://archive.apache.org/dist/tomcat/tomcat-7/v7.0.109/bin/apache-tomcat-7.0.109.tar.gz
tar --extract --file=apache-tomcat-7.0.109.tar.gz
chmod +x apache-tomcat-7.0.109/bin/catalina.sh

# download and unzip jOAI
curl --location --output joai_v3.3.zip https://github.com/NCAR/joai-project/releases/download/v3.3/joai_v3.3.zip
unzip joai_v3.3.zip

# copy the jOAI war-file to the appropriate Tomcat directory
cp joai_v3.3/oai.war apache-tomcat-7.0.109/webapps

# install the Caddy server, taken from https://caddyserver.com/docs/install#debian-ubuntu-raspbian
sudo apt install -y debian-keyring debian-archive-keyring apt-transport-https
curl -1sLf 'https://dl.cloudsmith.io/public/caddy/stable/gpg.key' | sudo gpg --dearmor -o /usr/share/keyrings/caddy-stable-archive-keyring.gpg
curl -1sLf 'https://dl.cloudsmith.io/public/caddy/stable/debian.deb.txt' | sudo tee /etc/apt/sources.list.d/caddy-stable.list
sudo apt update
sudo apt install caddy

# start Caddy, replace the domain name, taken from https://caddyserver.com/docs/quick-starts/reverse-proxy#caddyfile and https://caddyserver.com/docs/running#linux-service
sudo bash -c 'printf "oai.example.com\n\nreverse_proxy :8080\n" > /etc/caddy/Caddyfile'
sudo sudo systemctl enable --now caddy

# clone the Java harvester
git clone https://github.com/research-software-directory/rsd-oai-pmh.git

# build the harvester into a jar with all dependencies
cd rsd-oai-pmh && mvn package && cd ..

# make a directory for the XML files
mkdir rsd-xml-files 

# Add the Java harvester as cronjob, run it three times per day
(crontab -l ; echo "45 */8 * * * /usr/bin/java -cp /home/ubuntu/rsd-oai-pmh/target/rsd-oai-pmh-1.0-SNAPSHOT-jar-with-dependencies.jar nl.esciencecenter.DataciteDownloader /home/ubuntu/rsd-xml-files") | crontab -

# Run the Java harvester once manually
java -cp /home/ubuntu/rsd-oai-pmh/target/rsd-oai-pmh-1.0-SNAPSHOT-jar-with-dependencies.jar nl.esciencecenter.DataciteDownloader /home/ubuntu/rsd-xml-files
```

Now edit `apache-tomcat-7.0.109/conf/tomcat-users.xml` to add the role `oai_admin` and to set an admin user which also has that role `oai_admin`. You can do that with the following lines as children to the `tomcat-users` element, change the username and password:
```xml
<role rolename="manager-gui"/>
<role rolename="oai_admin"/>
<user username="username" password="password" roles="manager-gui,oai_admin"/>
```

```shell
# start Tomcat
echo 'export CATALINA_HOME=/home/ubuntu/apache-tomcat-7.0.109' >> ~/.bashrc
exec bash
echo 'export JAVA_HOME=/usr' >> /home/ubuntu/apache-tomcat-7.0.109/bin/setenv.sh
$CATALINA_HOME/bin/catalina.sh start
```

Now visit the app, click on `Manager App`, enter your credentials and deploy jOAI.

You should now be able to visit the app on the `/oai` path.

We now need restrict the jOAI admin interface to use credentials, edit `apache-tomcat-7.0.109/webapps/oai/WEB-INF/web.xml` to uncomment the elements `security-constraint` and `login-config` (they're at the bottom).
We also probably need to restart Tomcat:
```shell
$CATALINA_HOME/bin/catalina.sh stop
$CATALINA_HOME/bin/catalina.sh start
```

Visit the jOAI admin interface again, configure it and add the directory containing the XML files to the data provider.
