# ==============================
# Build stage
# ==============================
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .

RUN mvn dependency:go-offline -B

COPY src ./src

RUN mvn clean package -DskipTests


# ==============================
# Runtime stage
# ==============================
FROM eclipse-temurin:21-jre

WORKDIR /app

# Install FFmpeg and yt-dlp
RUN apt-get update \
    && apt-get install -y --no-install-recommends \
       ffmpeg \
       python3 \
       python3-pip \
    && pip3 install --break-system-packages yt-dlp \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# Copy Spring Boot JAR
COPY --from=build /app/target/*.jar app.jar

# Download directory
RUN mkdir -p /app/downloads

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]