FROM eclipse-temurin:17-jdk-alpine AS builder

RUN echo 'http://mirrors.aliyun.com/alpine/v3.18/main' > /etc/apk/repositories && \
    echo 'http://mirrors.aliyun.com/alpine/v3.18/community' >> /etc/apk/repositories && \
    apk add --no-cache maven

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B || mvn dependency:resolve -B

COPY src ./src
RUN mvn clean package -Dmaven.test.skip=true

FROM eclipse-temurin:17-jre-alpine

RUN apk add --no-cache wget

RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

WORKDIR /app

RUN mkdir -p /app/logs && chown -R appuser:appgroup /app

COPY --from=builder --chown=appuser:appgroup /app/target/*.jar app.jar

USER appuser

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=120s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-Xmx256m", "-jar", "-Djava.security.egd=file:/dev/./urandom", "app.jar"]
