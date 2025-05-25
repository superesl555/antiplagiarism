# AntiPlagiarism System

Система AntiPlagiarism предназначена для хранения файлов, их анализа и построения облака слов на основе содержимого. Состоит из трёх микросервисов, объединённых через API Gateway:

1. **File Storage Service** (`port 8080`) — хранит файлы в базе данных PostgreSQL, проверяет дубликаты.
2. **File Analysis Service** (`port 8080`) — анализирует текст (количество абзацев, слов, символов), генерирует URL облака слов через QuickChart API.
3. **API Gateway** (`port 8082`) — маршрутизирует запросы к соответствующим сервисам и выступает точкой входа для веб‑интерфейса.

## Getting Started

### Prerequisites

* Java 17+
* Maven 3.8+
* Docker (опционально) с PostgreSQL на порту `5542`:

  ```bash
  docker run -d \
    -e POSTGRES_DB=antiplagiarism_db \
    -e POSTGRES_USER=antiplagiarism_user \
    -e POSTGRES_PASSWORD=antiplagiarism_pass \
    -p 5542:5432 \
    --name pg_antiplagiarism postgres:15
  ```

### Environment Variables

Сервисы настраиваются через `application.properties`:

* **File Storage** (`src/main/resources/application.properties`):

  ```properties
  server.port=8080
  spring.datasource.url=jdbc:postgresql://localhost:5542/antiplagiarism_db
  spring.datasource.username=antiplagiarism_user
  spring.datasource.password=antiplagiarism_pass
  spring.jpa.hibernate.ddl-auto=update
  ```


* **API Gateway** (`src/main/resources/application.properties`):

  ```properties
  server.port=8082
  spring.cloud.gateway.routes[0].id=upload_route
  spring.cloud.gateway.routes[0].uri=http://localhost:8080
  spring.cloud.gateway.routes[0].predicates[0]=Path=/api/files
  spring.cloud.gateway.routes[0].filters[0]=StripPrefix=1

  spring.cloud.gateway.routes[1].id=download_route
  spring.cloud.gateway.routes[1].uri=http://localhost:8080
  spring.cloud.gateway.routes[1].predicates[0]=Path=/api/files/**
  spring.cloud.gateway.routes[1].filters[0]=StripPrefix=1

  management.endpoints.web.exposure.include=gateway,health,info
  ```

### Build & Run

1. **Build all modules**:

   ```bash
   mvn clean install
   ```
2. **Запуск**:

    * File Storage & File Analysis: `mvn spring-boot:run -pl file-storage`
    * API Gateway:    `mvn spring-boot:run -pl api-gateway`
3. **Открыть веб-интерфейс** в браузере: `http://localhost:8082/index.html`

## API Specification

### 1. Upload File

* **Endpoint:** `POST /api/files`
* **Content-Type:** `multipart/form-data`
* **Request Body:** поле `file` — загружаемый файл
* **Responses:**

    * `201 Created` — возвращает JSON с информацией о файле:

      ```json
      {
        "id": 1,
        "filename": "example.txt",
        "contentType": "text/plain",
        "uploadedAt": "2025-05-25T14:22:00"
      }
      ```
    * `409 Conflict` — файл уже существует (проверка по содержимому):

      ```json
      { "error": "Duplicate file" }
      ```

### 2. Download File

* **Endpoint:** `GET /api/files/{id}`
* **Responses:**

    * `200 OK` — возвращает бинарный контент с заголовками `Content-Type` и `Content-Disposition`.

### 3. Analyze File

* **Endpoint:** `POST /api/analysis/{fileId}`
* **Content-Type:** `application/octet-stream`
* **Request Body:** двоичные данные файла (можно использовать результат `GET /api/files/{id}`)
* **Responses:**

    * `200 OK` — JSON с результатами анализа и URL облака слов:

      ```json
      {
        "paragraphs": 5,
        "words": 120,
        "characters": 650,
        "cloudUrl": "https://quickchart.io/wordcloud?text=..."
      }
      ```
    * `404 Not Found` — файл не найден
