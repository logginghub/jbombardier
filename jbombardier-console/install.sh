#!/bin/bash
echo Starting the install script
pwd
echo Stopping the old version
cd /tmp
pwd
cd agent
cd bin
./agent.sh stop

echo Deleting the old version
cd /tmp
rm -rf agent
mkdir agent
unzip vertexlabs-performance-agent-linux.zip -d agent
cd agent
cd bin
chmod +x agent.sh
./agent.sh start