FROM amazoncorretto:21-alpine

RUN apk add --no-cache curl tzdata && \
    addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

WORKDIR /app

COPY /build/libs/app-web-1.0.0.jar app.jar

RUN chown -R appuser:appgroup /app

USER appuser

EXPOSE 8300

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8300/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java -jar app.jar"]
