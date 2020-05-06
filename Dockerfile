#
# Build image
#
FROM maven:3-openjdk-8 as builder
WORKDIR /project
COPY src/ src/
COPY pom.xml ./
RUN mvn clean package

#
# Runtime container image
#
FROM openjdk:8-jre-slim

# Non-root user
RUN groupadd -g 10101 app && \
    useradd -m -d /project -g app -u 10101 app
USER app:app
WORKDIR /project

# App from the build output
COPY --from=builder /project/target/streams-demo-0.1.jar /streams-demo.jar
COPY entrypoint.sh /entrypoint.sh
ENTRYPOINT [ "/entrypoint.sh", "/streams-demo.jar" ]
