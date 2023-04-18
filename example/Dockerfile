FROM openjdk:8-jdk-alpine
MAINTAINER www.byteblogs.com

ADD ./target/easy-retry-example.jar easy-retry-example.jar

#对外暴漏的端口号
EXPOSE 8088

WORKDIR /

#开机启动
ENTRYPOINT ["sh","-c","java -jar $JAVA_OPTS /easy-retry-example.jar $PARAMS"]

