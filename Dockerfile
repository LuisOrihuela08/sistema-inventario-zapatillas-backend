FROM amazoncorretto:17-alpine-jdk
WORKDIR /app
EXPOSE 8080
COPY ./target/sistema-inventario-zapatillas-0.0.1-SNAPSHOT.jar sistema-inventario-back.jar
ENTRYPOINT [ "java", "-jar", "sistema-inventario-back.jar"]