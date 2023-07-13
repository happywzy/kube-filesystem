FROM hub.deri.org.cn/library/openjdk:8-jdk-alpine
MAINTAINER wuzhiyong Docker Maintainers "wuzhiyong@deri.energy"

ADD target/filesystem-1.0.0-SNAPSHOT.jar  /root

EXPOSE 8080
ENV LANG C.UTF-8
CMD cd /root && java -jar filesystem-1.0.0-SNAPSHOT.jar