FROM --platform=$BUILDPLATFORM amazoncorretto:17 as builder
WORKDIR /

ADD ./target/snail-job-server-exec.jar snail-job-server.jar

FROM --platform=$BUILDPLATFORM amazoncorretto:17
MAINTAINER opensnail
LABEL server-name=snail-job-server

#对外暴漏的端口号
EXPOSE 8080
EXPOSE 1788

WORKDIR /
COPY --from=builder /snail-job-server.jar .

ENTRYPOINT ["sh","-c","java -jar $JAVA_OPTS /snail-job-server.jar $PARAMS"]
