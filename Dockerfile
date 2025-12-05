FROM eclipse-temurin:17-jdk
ARG JAR_FILE=*.jar

RUN apt-get update && apt-get install -y tzdata
ENV TZ=Asia/Seoul

COPY build/libs/snapmeal-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]


