# 拉取jdk8作为基础镜像
FROM openjdk:17
# 作者
MAINTAINER Xiwang <xiw@xiw.com>
# 添加jar到镜像并命名为kuwei.jar
ADD target/kuwei-1.0-SNAPSHOT.jar kuwei.jar

RUN /bin/cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime

EXPOSE 8080
# 28080为调试端口
EXPOSE 28080
# jar运行命令，参数使用逗号隔开
ENTRYPOINT ["java","-Xdebug" ,"-Xrunjdwp:transport=dt_socket,suspend=n,server=y,address=*:28080","-jar","kuwei.jar"]
