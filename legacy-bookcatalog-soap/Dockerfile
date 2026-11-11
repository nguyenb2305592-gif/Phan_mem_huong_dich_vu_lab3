# Image dịch vụ SOAP kế thừa BookCatalogService (cổng 8091) — build nhiều giai đoạn
# docker build -t bookstore/legacy-bookcatalog-soap:1.0 legacy-bookcatalog-soap
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B -DskipTests package

FROM eclipse-temurin:21-jre
RUN useradd --system --uid 10001 legacy
WORKDIR /app
COPY --from=build /src/target/legacy-bookcatalog-soap-*-all.jar /app/legacy.jar
USER legacy
EXPOSE 8091
ENV LEGACY_PORT=8091 LEGACY_DELAY_MS=0 LEGACY_DUMP=true
HEALTHCHECK --interval=10s --timeout=3s --retries=10 \
  CMD curl -fs "http://localhost:8091/ws/catalog?wsdl" > /dev/null || exit 1
ENTRYPOINT ["java", "-jar", "/app/legacy.jar"]
