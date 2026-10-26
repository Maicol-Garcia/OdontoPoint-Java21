FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn -B clean package -DskipTests

FROM eclipse-temurin:21-jre-jammy

RUN apt-get update \
    && apt-get install -y --no-install-recommends \
       fontconfig fonts-dejavu-core \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY --from=build \
    /app/target/odontopoint-web-2.0.0.jar \
    /app/odontopoint.jar

ENV JAVA_TOOL_OPTIONS="-Xmx256m -Djava.awt.headless=true"

EXPOSE 10000

ENTRYPOINT ["java", "-jar", "/app/odontopoint.jar"]