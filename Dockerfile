FROM alpine/git
RUN mkdir -p /opt
WORKDIR /opt
RUN git clone https://github.com/shiruka/shiruka.git

FROM adoptopenjdk/maven-openjdk11
RUN mkdir -p /opt/shiruka
WORKDIR /opt/shiruka
COPY --from=0 /opt/shiruka /opt/shiruka
RUN mvn clean install -Dmaven.javadoc.skip=true -Dmaven.source.skip=true

FROM adoptopenjdk/openjdk11:alpine
WORKDIR /app
COPY --from=1 /opt/shiruka/target/Shiruka.jar /opt/shiruka
COPY --from=1 /opt/shiruka/entrypoint.sh /opt/shiruka
EXPOSE 19132
ENTRYPOINT ["/bin/sh", "/opt/shiruka/entrypoint.sh"]
CMD [""]