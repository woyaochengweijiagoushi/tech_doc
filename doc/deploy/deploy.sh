#!/bin/bash
echo "publish----------"

process_id=`ps -ef | grep tech_doc.jar | grep -v grep |awk '{print $2}'`
if [ $process_id ] ; then
sudo kill -9 $process_id
fi

source /etc/profile
nohup java -jar -Dspring.profiles.active=prod /opt/juege/tech_doc/tech_doc.jar > /dev/null 2>&1 &

echo "end publish"
