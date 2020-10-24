FROM  openjdk:11-jdk-slim AS build
COPY . /home/src
WORKDIR /home/src
RUN chmod +x gradlew
RUN ./gradlew --no-daemon build

FROM openjdk:11-jre-slim
RUN mkdir /app
COPY --from=build /home/src/build/libs/*.jar /app/restcli.jar
ENTRYPOINT ["java", "-jar","/app/restcli.jar"]