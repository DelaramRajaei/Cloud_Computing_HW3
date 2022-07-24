#!/bin/bash

/etc/init.d/ssh start

# format namenode
# $HADOOP_HOME/bin/hdfs namenode -format

# start hadoop
# $HADOOP_HOME/sbin/start-dfs.sh
# $HADOOP_HOME/sbin/start-yarn.sh
# $HADOOP_HOME/sbin/mr-jobhistory-daemon.sh start historyserver

mkdir -p ~/.ssh/
cat /id_rsa.pub > ~/.ssh/authorized_keys