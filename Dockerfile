## Use the official OpenJDK image as the base image
#FROM openjdk:17
## build가 되는 시점에 JAR_FILE 이라는 변수명에 build/libs/*.jar 표현식을 선언했다는 의미입니다.
## build/libs 경로는 gradle로 빌드했을 때 jar 파일이 생성되는 경로입니다.
#ARG JAR_FILE=build/libs/*.jar
## 위에 선언한 JAR_FILE 을 app.jar 로 복사합니다.
#COPY ${JAR_FILE} app.jar
## jar 파일을 실행하는 명령어(java -jar jar파일) 입니다.
#ENTRYPOINT ["java","-jar","/app.jar"]
# Use the official OpenJDK 17 image as the base image
FROM openjdk:17

# Set the working directory
WORKDIR /app

# Build argument for the JAR file location
ARG JAR_FILE=build/libs/*.jar

# Copy the JAR file to the container
COPY ${JAR_FILE} app.jar

# Install Python and required packages
RUN apt-get update && \
    apt-get install -y python3 python3-venv python3-pip && \
    python3 -m venv venv

# Activate virtual environment and install NLTK
RUN . venv/bin/activate && \
    pip install --upgrade pip && \
    pip install nltk && \
    python -m nltk.downloader vader_lexicon punkt

# Set environment variables for Python virtual environment
ENV PATH="/app/venv/bin:$PATH"

# Copy Python scripts to the container
COPY src/main/resources/python /app/python

# Run the Spring Boot application
ENTRYPOINT ["java","-jar","/app.jar"]
