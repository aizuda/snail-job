FROM openjdk:8-jdk-alpine
MAINTAINER www.byteblogs.com

ADD ./target/x-retry-example.jar x-retry-example.jar

#对外暴漏的端口号
EXPOSE 8088

WORKDIR /

#开机启动
ENTRYPOINT ["sh","-c","java -jar $JAVA_OPTS /x-retry-example.jar $PARAMS"]

