FROM registry-push.idst.ibaintern.de:5050/public/ibi/openjdk-jre:11-sles
#FROM openjdk:8

ENV JAVA_OPTS="-XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap"

ADD target/oracle-db-operator-*.jar /oracle-db-operator.jar

CMD ["java", "-jar", "/oracle-db-operator.jar"]
