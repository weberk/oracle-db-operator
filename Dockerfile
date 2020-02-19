FROM openjdk:8

ENV JAVA_OPTS="-XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap"

ADD target/db-example-operator-*.jar /db-example-operator.jar

CMD ["java", "-jar", "/db-example-operator.jar"]
