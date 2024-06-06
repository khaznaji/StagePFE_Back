FROM openjdk:11
EXPOSE 8080
ADD target/Backend-1.0.jar Backend-1.0.jar
ENTRYPOINT ["java","-jar","/Backend-1.0.jar"]