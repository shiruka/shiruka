FROM alpine/git
RUN mkdir -p /app
WORKDIR /app
RUN git clone https://github.com/shiruka/shiruka.git

FROM maven:3.6.3-jdk-11
WORKDIR /app
COPY --from=0 /app/shiruka /app
RUN mvn clean install -Pcheckstyle -Dmaven.javadoc.skip=true -Dmaven.source.skip=true

FROM adoptopenjdk/openjdk11:alpine
WORKDIR /app
COPY --from=1 /app/target/Shiruka.jar /app
EXPOSE 19132
CMD ["java", "-jar", "Shiruka.jar"]