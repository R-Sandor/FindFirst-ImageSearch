FROM openjdk:17
WORKDIR /app
COPY ./build/libs/bookmarkit-1.0.0.jar bookmarkit.jar
ENV pgsql-datasource "jdbc:postgresql://db:5432/postgres"
ENV es-host "es01"
ENTRYPOINT ["java", "-Delastic.host=${es-host}", "-Dspring.datasource.url=${pgsql-datasource}",  "-jar", "bookmarkit.jar"]
