FROM docker.io/library/node:lts as js-builder

COPY src/main/resources/static/package.json /build/

WORKDIR /build/

RUN : \
    && npm install \
    && :

################################################################################

FROM docker.io/library/maven:3-eclipse-temurin-17 as builder

COPY pom.xml /build/pom.xml
COPY src /build/src
COPY --from=js-builder /build/node_modules/ /build/src/main/resources/static/node_modules/

WORKDIR /build

RUN : \
    && mvn -B -DskipTests package spring-boot:repackage \
    && :

################################################################################

FROM docker.io/library/eclipse-temurin:17-jre

WORKDIR /app

COPY --from=builder /build/target/*.jar /app/scratchlog.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/scratchlog.jar"]
