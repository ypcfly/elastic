FROM openjdk:8-jdk-alpine
ENV TIME_ZONE=Asia/Shanghai
RUN echo -e http://mirrors.ustc.edu.cn/alpine/v3.9/main/ > /etc/apk/repositories \
    && echo "${TIME_ZONE}" > /etc/timezone \
    && apk add --no-cache tzdata \
    && ln -sf /usr/share/zoneinfo/${TIME_ZONE} /etc/localtime
VOLUME /tmp
ADD target/elastic-1.0-SNAPSHOT.jar app.jar
ENTRYPOINT exec java ${JAVA_OPTS} -Djava.security.egd=file:/dev/./urandom -jar /app.jar
