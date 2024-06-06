FROM openjdk:11
EXPOSE 8080
ADD target/stagepfe-1.0.jar stagepfe-1.0.jar
ENTRYPOINT ["java","-jar","/stagepfe-1.0.jar"]