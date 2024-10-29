FROM bellsoft/liberica-openjdk-debian:17.0.11-cds

LABEL maintainer="opensnail"
LABEL app-name="snail-job-server"
LABEL description="SnailJob is a high performance distributed task scheduler and retry management center"


ENV TZ=Asia/Shanghai LANG=C.UTF-8 LC_ALL=C.UTF-8
ENV JAVA_OPTS="-Djava.security.egd=file:/dev/./urandom -XX:+HeapDumpOnOutOfMemoryError -XX:+UseZGC"
ENV JAVA_OPTS=""

WORKDIR /snailjob/server
ADD ./target/snail-job-server-exec.jar app.jar

# 对外暴漏的端口号
EXPOSE 8080
EXPOSE 1788

# 数据卷, 用于映射日志目录
VOLUME /snailjob/server/data

ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar app.jar $PARAMS" ]
