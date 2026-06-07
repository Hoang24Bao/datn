# Bước 1: Build file JAR từ mã nguồn Java Spring Boot
FROM maven:3.8.8-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Bước 2: Tạo môi trường chạy ứng dụng chứa cả Java và Python
FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app

# Cài đặt Python 3 và thư viện gTTS để xử lý âm thanh tiếng Nhật
RUN apt-get update && apt-get install -y python3 python3-pip && rm -rf /var/lib/apt/lists/*
RUN pip3 install gtts --break-system-packages

# Copy file .jar đã build thành công vào môi trường chạy
COPY --from=build /app/target/*.jar app.jar

# Mở cổng 8080 cho ứng dụng Web
EXPOSE 8080

# Lệnh khởi chạy chính
ENTRYPOINT ["java", "-jar", "app.jar"]