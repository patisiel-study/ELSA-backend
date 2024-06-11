# Use the official OpenJDK image as the base image
FROM openjdk:17-slim

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
ENTRYPOINT ["java","-jar","/app/app.jar"]
