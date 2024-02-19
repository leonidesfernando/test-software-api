# To run without build on Docker
#FROM eclipse-temurin:17-jdk-alpine
#VOLUME /tmp
#EXPOSE 8080
#ARG JAR_FILE=target/teste-software-0.0.1-SNAPSHOT.jar
#ADD ${JAR_FILE} app.jar
#ENTRYPOINT ["java","-jar","/app.jar"]

#building on Docker
FROM eclipse-temurin:17-jdk-alpine AS MAVEN_BUILD

RUN apk add --no-cache bash procps curl tar

# common for all images
ENV MAVEN_HOME /usr/share/maven

COPY --from=maven:3.9.4-eclipse-temurin-11 ${MAVEN_HOME} ${MAVEN_HOME}
COPY --from=maven:3.9.4-eclipse-temurin-11 /usr/local/bin/mvn-entrypoint.sh /usr/local/bin/mvn-entrypoint.sh
COPY --from=maven:3.9.4-eclipse-temurin-11 /usr/share/maven/ref/settings-docker.xml /usr/share/maven/ref/settings-docker.xml

RUN ln -s ${MAVEN_HOME}/bin/mvn /usr/bin/mvn

ARG MAVEN_VERSION=3.9.4
ARG USER_HOME_DIR="/root"
ENV MAVEN_CONFIG "$USER_HOME_DIR/.m2"

ENTRYPOINT ["/usr/local/bin/mvn-entrypoint.sh"]
CMD ["mvn"]

COPY pom.xml /build/
COPY src /build/src/

WORKDIR /build/
RUN mvn package


FROM eclipse-temurin:17-jdk-alpine

WORKDIR /api
EXPOSE 8080
COPY --from=MAVEN_BUILD /build/target/test-software-api-0.0.1-SNAPSHOT.jar /api/api.jar

ENTRYPOINT ["java", "-jar", "app.jar"]