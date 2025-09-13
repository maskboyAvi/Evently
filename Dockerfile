FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline || true
COPY src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
# Create non-root user for better security
RUN useradd -ms /bin/bash appuser
USER appuser
ENV JAVA_OPTS="-XX:+UseContainerSupport -Xms256m -Xmx512m"
ENV SPRING_PROFILES_ACTIVE=prod
ENV APP_CORS_ORIGINS="*"
COPY --from=build /app/target/*-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar app.jar"]