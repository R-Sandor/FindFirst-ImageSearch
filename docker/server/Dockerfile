FROM openjdk:17
WORKDIR /app
COPY ./build/libs/bookmarkit-1.0.0.jar bookmarkit.jar
ENV pgsql-datasource "jdbc:postgresql://db:5432/postgres"
ENV es-host "es01"
ENV pytorch-url "http://pytorch:5000/"
ENTRYPOINT ["java", "-Delastic.host=${es-host}", "-Dspring.datasource.url=${pgsql-datasource}", "Dpytorch.url=${pytorch-url}" "-jar", "bookmarkit.jar"]
