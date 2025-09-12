FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline || true
COPY src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
ENV JAVA_OPTS="-XX:+UseContainerSupport -Xms256m -Xmx512m"
ENV SPRING_PROFILES_ACTIVE=prod
ENV APP_JWT_SECRET=ZG1vX3NlY3JldF9mb3JfcmVuZGVyX2RlcGxveV9ldmVudGx5X2JhY2tlbmRfMjU2Yml0
ENV APP_CORS_ORIGINS=*
COPY --from=build /app/target/*-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar app.jar"]
# Multi-stage: build + slim runtime
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn -q -DskipTests package

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
# Create non-root user
RUN useradd -ms /bin/bash appuser
USER appuser
COPY --from=build /app/target/evently-backend-0.1.0-SNAPSHOT.jar app.jar
EXPOSE 8080
# Spring will pick datasource from env vars
ENTRYPOINT ["java","-jar","/app/app.jar"]