FROM ubuntu:22.04

ENV HADOOP_HOME /hadoop
ENV JAVA_HOME /usr/lib/jvm/java-8-openjdk-amd64
ENV HADOOP_USER=root

RUN apt-get update && apt-get -y install \
     ssh\
    openjdk-8-jdk
RUN wget https://dlcdn.apache.org/hadoop/common/hadoop-3.2.3/hadoop-3.2.3.tar.gz
RUN tar xzf hadoop-3.2.3.tar.gz && \
    mv /hadoop-3.2.3 $HADOOP_HOME && \
    echo "export JAVA_HOME=$JAVA_HOME" >> $HADOOP_HOME/etc/hadoop/hadoop-env.sh && \
    echo "PATH=$PATH:$HADOOP_HOME/bin" >> ~/.bashrc &&\
    echo -e "worker-1\nworker-2" >> $HADOOP_HOME/etc/hadoop/workers

RUN ssh-keygen -t rsa -P '' -f ~/.ssh/id_rsa && \
  cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys && \
  chmod 0600 ~/.ssh/authorized_keys
# RUN ssh-copy-id -i ~/.ssh/id_rsa.pub root@worker1
# RUN ssh-copy-id -i ~/.ssh/id_rsa.pub root@worker2


ENV HDFS_NAMENODE_USER=$HADOOP_USER
ENV HDFS_DATANODE_USER=$HADOOP_USER
ENV HDFS_SECONDARYNAMENODE_USER=$HADOOP_USER
ENV YARN_RESOURCEMANAGER_USER=$HADOOP_USER
ENV YARN_NODEMANAGER_USER=$HADOOP_USER


RUN echo "export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64\n\
export HADOOP_INSTALL=/hadoop\n\
export PATH=$PATH:$HADOOP_INSTALL/bin\n\
export PATH=$PATH:$HADOOP_INSTALL/sbin\n\
export HADOOP_MAPRED_HOME=$HADOOP_INSTALL \n\
export HADOOP_COMMON_HOME=$HADOOP_INSTALL \n\
export HADOOP_HDFS_HOME=$HADOOP_INSTALL \n\
export YARN_HOME=$HADOOP_INSTALL \n\
export HADOOP_COMMON_LIB_NATIVE_DIR=$HADOOP_INSTALL/lib/native\n\
export HADOOP_OPTS=\"-Djava.library.path=$HADOOP_INSTALL/lib\"\n\
export HADOOP_USER=root\n \
export HDFS_NAMENODE_USER=$HADOOP_USER \n \
export HDFS_DATANODE_USER=$HADOOP_USER \n \
export HDFS_SECONDARYNAMENODE_USER=$HADOOP_USER \n \
export YARN_RESOURCEMANAGER_USER=$HADOOP_USER \n \
export YARN_NODEMANAGER_USER=$HADOOP_USER " >> ~/.bashrc

RUN cat ~/.ssh/id_rsa.pub > /id_rsa.pub

ADD configs/*xml $HADOOP_HOME/etc/hadoop/
COPY configs/ssh_config /root/.ssh/config
COPY start-hadoop.sh start-hadoop.sh
COPY start-primary.sh start-primary.sh
CMD tail -f /dev/null