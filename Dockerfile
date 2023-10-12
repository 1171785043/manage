FROM java:8

MAINTAINER Yuming Ma

RUN mkdir -p /hospital

ENV SERVER_PORT=8080

EXPOSE ${SERVER_PORT}

ENTRYPOINT exec java -Dfile.encoding=utf-8 -Dserver.port=${SERVER_PORT} -jar -Duser.timezone=GMT+8 -Xms1024m -Xmx2048m -XX:PermSize=1024M -XX:MaxPermSize=2048M /hospital/hospital-back.jar
