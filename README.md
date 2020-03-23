# oracle-db-operator

[![License](https://img.shields.io/badge/license-Apache--2.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)

Kubernetes native approach for lifecycle management of Oracle pluggable databases inside of a Oracle container (multitenant) database, based on kubernetes operator and custom ressource definitions (CRD). It enables seamless integration of DevOps pipelines for microservices using Oralce polyglot databases, with Oracle autonomous database technology

# Architecture
TBD

# Required docker images

# Oracle Database
Build a docker image as described in https://github.com/oracle/docker-images/blob/master/OracleDatabase/SingleInstance/README.md. After cloning the git repository, download the database binaries in the version you need from [Oracle Technology Network](http://www.oracle.com/technetwork/database/enterprise-edition/downloads/index.html), put them in the dockerfiles/version folder, go in the dockerfiles directory and execute e.g.:
```bash
./buildDockerImage.sh -e -v 19.3.0
```
Then tag the image and push it to your repo, e.g.:
```bash
docker tag oracle/database:19.3.0-ee fra.ocir.io/oraseemeadesandbox/pdos/oracle/database:19.3.0-ee
```
```bash
docker push fra.ocir.io/oraseemeadesandbox/pdos/oracle/database:19.3.0-ee
```
# Oracle Rest Data Services (ORDS)
Build a docker image as described in https://github.com/oracle/docker-images/tree/master/OracleRestDataServices. The Dockerfile for ORDS depends on an Oracle Java 8 docker image, which should be build first. This can be avoided if following statement in the Dockerfile
```bash
FROM oracle/serverjre:8
```
is replaced by
```bash
FROM openjdk:8
```
After cloning the git repository, download the ORDS binaries in the version you need from [Oracle Technology Network](http://www.oracle.com/technetwork/developer-tools/rest-data-services/downloads/index.html), put them in the dockerfiles/version folder(do not unzip!), go in the dockerfiles directory and execute e.g.:
```bash
./buildDockerImage.sh
```
Then tag the image and push it to your repo, e.g.:
```bash
docker tag oracle/restdataservices:19.2.0 fra.ocir.io/oraseemeadesandbox/pdos/oracle/restdataservices:19.2.0
```
```bash
docker push fra.ocir.io/oraseemeadesandbox/pdos/oracle/restdataservices:19.2.0
```
# Oracle Database Operator
To build the docker image, the make utility, java jdk8 and maven > 3.0.0 are needed. Clone this git repository, cd into it and execute:
```bash
make build
```
Then tag and push the docker image to your repository:
```bash
docker tag oracle/oracle-db-operator:latest fra.ocir.io/oraseemeadesandbox/pdos/oracle/oracle-db-operator:latest
```
```bash
docker push fra.ocir.io/oraseemeadesandbox/pdos/oracle/oracle-db-operator:latest
```
# Deployment

Create a registry secret to hold the docker credentials for your docker repository
```bash
kubectl create -f manifest/operator.yaml
```

# Credits
This project is based on the generic code for [java-based kubernetes operators](https://github.com/jvm-operators/java-example-operator), the [abstract-operator](https://github.com/jvm-operators/abstract-operator) library and some modifications and cleanup of a similar project https://github.com/malagoli/oracle-db-operator
