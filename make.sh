#!/bin/sh
export IMAGE="oracle/oracle-db-operator"

export MAVEN_OPTS="-Djansi.passthrough=true -Dplexus.logger.type=ansi $MAVEN_OPTS" 
#sh ./mvnw clean package -DskipTests
mvn clean install
docker.exe build -t $IMAGE:latest -f Dockerfile .

