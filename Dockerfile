## Use the official Debian-based OpenJDK image as the base image
#FROM openjdk:17-alpine
#
## Set the working directory
#WORKDIR /app
#
## Build argument for the JAR file location
#ARG JAR_FILE=build/libs/*.jar
#
## Copy the JAR file to the container
#COPY ${JAR_FILE} app.jar
#
### Install Python and required packages
##RUN apt-get update && \
##    apt-get install -y python3 python3-venv python3-pip && \
##    python3 -m venv venv
##
### Activate virtual environment and install NLTK
##RUN . venv/bin/activate && \
##    pip install --upgrade pip && \
##    pip install nltk && \
##    python -m nltk.downloader vader_lexicon punkt
#
## Set environment variables for Python virtual environment
#ENV PATH="/app/venv/bin:$PATH"
#ENV JAVA_OPTS="-Xms512m -Xmx1024m"
#
## Copy Python scripts to the container
##COPY src/main/resources/python /app/python
#
## Run the Spring Boot application
#ENTRYPOINT ["java", "-jar", "/app/app.jar"]
##CMD ["--spring.config.additional-location=optional:/app/config/application.yml"]

## Use a stable Debian-based OpenJDK image as the base image
#FROM openjdk:17-jdk-slim
#
## Set the working directory
#WORKDIR /app
#
## Build argument for the JAR file location
#ARG JAR_FILE=build/libs/*.jar
#
## Copy the JAR file to the container
#COPY ${JAR_FILE} app.jar
#
## Set environment variables for Java options
#ENV JAVA_OPTS="-Xms512m -Xmx1024m"
## Run the Spring Boot application
#ENTRYPOINT ["java", "-jar", "/app/app.jar"]
# OpenJDK 17을 베이스 이미지로 사용
FROM openjdk:17

# 애플리케이션 JAR 파일을 컨테이너의 루트 디렉토리로 복사
COPY build/libs/*.jar app.jar

# JVM 옵션을 환경 변수로 설정 (메모리 설정 포함)
ENV JAVA_OPTS="-Xms512m -Xmx2048m"

# Java 애플리케이션을 실행
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]