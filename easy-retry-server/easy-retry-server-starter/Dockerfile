FROM --platform=$BUILDPLATFORM amazoncorretto:17 as builder
WORKDIR /

ADD ./target/easy-retry-server-exec.jar easy-retry-server.jar

FROM --platform=$BUILDPLATFORM amazoncorretto:17
MAINTAINER www.byteblogs.com
LABEL server-name=easy-retry-server

#对外暴漏的端口号
EXPOSE 8080
EXPOSE 1788

WORKDIR /
COPY --from=builder /easy-retry-server.jar .

ENTRYPOINT ["sh","-c","java -jar $JAVA_OPTS /easy-retry-server.jar $PARAMS"]
