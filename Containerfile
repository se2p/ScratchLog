FROM docker.io/library/maven:3-eclipse-temurin-21 as builder

COPY pom.xml /build/pom.xml
COPY src /build/src

WORKDIR /build

RUN : \
    && mvn -B -DskipTests package spring-boot:repackage \
    && :

################################################################################

FROM docker.io/library/eclipse-temurin:25-jre

WORKDIR /app

COPY --from=builder /build/target/*.jar /app/scratchlog.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/scratchlog.jar"]
