FROM openjdk:8-jdk-alpine
MAINTAINER www.byteblogs.com
LABEL server-name=easy-retry-server

ADD ../target/easy-retry-server.jar easy-retry-server.jar

#对外暴漏的端口号
EXPOSE 8080
EXPOSE 1788

WORKDIR /

ENTRYPOINT ["sh","-c","java -jar $JAVA_OPTS /easy-retry-server.jar $PARAMS"]
