# 拉取jdk17作为基础镜像
FROM eclipse-temurin:17-jre
# 作者
MAINTAINER Xiwang <xiw@xiw.com>

# 设置时区为东八区（完整方式）
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

COPY target/kuwei-1.0-SNAPSHOT.jar app.jar

EXPOSE 8080 28080
ENTRYPOINT ["java", "-Xdebug", "-Xrunjdwp:transport=dt_socket,suspend=n,server=y,address=*:28080", "-jar", "app.jar"]