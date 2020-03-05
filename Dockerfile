FROM openjdk:8

ENV JAVA_OPTS="-XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap"

ADD target/oracle-db-operator-*.jar /oracle-db-operator.jar

CMD ["java", "-jar", "/oracle-db-operator.jar"]
