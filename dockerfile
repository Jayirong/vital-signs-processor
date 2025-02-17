    FROM eclipse-temurin:21-jdk

    WORKDIR /app

    COPY  target/vital-signs-processor-0.0.1-SNAPSHOT.jar app.jar

    EXPOSE 8088

    ENTRYPOINT [ "java", "-jar", "app.jar" ]