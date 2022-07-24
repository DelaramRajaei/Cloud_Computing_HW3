#!/bin/bash


# format namenode
$HADOOP_HOME/bin/hdfs namenode -format

# start hadoop
$HADOOP_HOME/sbin/start-dfs.sh
$HADOOP_HOME/sbin/start-yarn.sh
$HADOOP_HOME/bin/hadoop fs -mkdir -p /user/hadoop/input
$HADOOP_HOME/bin/hadoop fs -put /datasets/*.csv /user/hadoop/input 
# $HADOOP_HOME/sbin/mr-jobhistory-daemon.sh start historyserver
