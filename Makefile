IMAGE?=oracle/oracle-db-operator

.PHONY: build
build: package image-build

.PHONY: package
package:
	MAVEN_OPTS="-Djansi.passthrough=true -Dplexus.logger.type=ansi $(MAVEN_OPTS)" sh ./mvnw clean package -DskipTests

.PHONY: test
test:
	MAVEN_OPTS="-Djansi.passthrough=true -Dplexus.logger.type=ansi $(MAVEN_OPTS)" sh ./mvnw clean test

.PHONY: image-build
image-build:
	docker build -t $(IMAGE):latest -f Dockerfile .

