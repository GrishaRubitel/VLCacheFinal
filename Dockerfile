FROM gradle:jdk17 as build
COPY src /home/gradle/src
COPY build.gradle /home/gradle/
COPY settings.gradle /home/gradle/
RUN gradle bootJar

FROM openjdk:17.0.2-jdk-bullseye

RUN curl -L https://github.com/yt-dlp/yt-dlp/releases/download/2024.07.01/yt-dlp_linux -o /usr/local/bin/yt-dlp
RUN chmod 777 /usr/local/bin/yt-dlp

COPY --from=build /home/gradle/build/libs/VLCacheFinal-0.0.1-SNAPSHOT.jar /usr/local/lib/demo.jar
EXPOSE 8100
ENTRYPOINT java -jar /usr/local/lib/demo.jar
