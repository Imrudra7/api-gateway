# STAGE 1: Build the JAR using Maven
FROM maven:3.9.11-eclipse-temurin-21 AS build

ARG GITHUB_USERNAME
ARG GITHUB_TOKEN

WORKDIR /app

RUN mkdir -p /root/.m2

RUN printf '<settings>\
<servers>\
<server>\
<id>github</id>\
<username>%s</username>\
<password>%s</password>\
</server>\
</servers>\
</settings>' "$GITHUB_USERNAME" "$GITHUB_TOKEN" > /root/.m2/settings.xml

# Copy the pom.xml and download dependencies (cached for speed)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the source code and build the package
COPY src ./src
RUN mvn clean package -DskipTests

# STAGE 2: Create the final small image
FROM eclipse-temurin:21-jdk
WORKDIR /app

# Copy ONLY the JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]