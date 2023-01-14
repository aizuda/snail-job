FROM openjdk:8-jdk-alpine
MAINTAINER www.byteblogs.com
LABEL server-name=x-retry-server

ADD ./target/x-retry-server.jar x-retry-server.jar

#对外暴漏的端口号
EXPOSE 8080
EXPOSE 1788

WORKDIR /

ENTRYPOINT ["sh","-c","java -jar $JAVA_OPTS /x-retry-server.jar $PARAMS"]
