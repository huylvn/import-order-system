FROM maven:3.9.9-eclipse-temurin-21 AS build

RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        xvfb \
        libgtk-3-0 \
        libx11-6 \
        libxtst6 \
        libxi6 \
        libxrender1 \
        libxxf86vm1 \
        libgl1 \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /workspace

COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN xvfb-run -a mvn -B test package
