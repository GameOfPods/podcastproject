FROM maven:3.9.7-eclipse-temurin-21-alpine as build

RUN mkdir /src
COPY . /src
WORKDIR /src
RUN mvn clean package -Pproduction

#FROM eclipse-temurin:21-jre
#COPY --from=build /src/target/*.jar app.jar
EXPOSE 8080
ENV PORT=8080
ENV PODCAST_PROJECT_CONFIG=/config
ENTRYPOINT ["java", "-jar", "/src/target/podcastproject.jar"]
