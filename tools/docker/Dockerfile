FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY docs ./docs
COPY src ./src
RUN mvn -B clean package -DskipTests

FROM eclipse-temurin:21-jre-jammy
COPY --from=build /app/target/padelcenter-*.jar /padelcenter.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/padelcenter.jar"]
