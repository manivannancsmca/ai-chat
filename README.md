# AI Chat — How to Run

Step-by-step guide to run the Spring AI Text & Voice Chat application with Ollama and Llama 3.2.

This document covers setup, running locally, running with Docker, using the UI and APIs, and common problems. It does not include source code.

---

## What this application does

- Text chat with a local LLM (Llama 3.2 via Ollama)
- Streaming text responses
- Voice chat (upload audio → speech-to-text → AI → text-to-speech)
- Web UI in the browser
- Health checks and API docs (Swagger) when enabled

Default ports:

| Service        | Port  |
|----------------|-------|
| Spring Boot app| 8080  |
| Ollama         | 11434 |

---

## 1. Prerequisites

Install and verify:

| Tool              | Minimum        | Check command              |
|-------------------|----------------|----------------------------|
| JDK               | 21             | `java -version`            |
| Maven             | 3.9+           | `mvn -version`             |
| Docker            | 24+ (optional) | `docker --version`         |
| Docker Compose    | v2 (optional)  | `docker compose version`   |
| Browser           | Chrome or Edge | For UI and microphone      |

Optional: `curl` or Postman for API testing.

---

## 2. Decide how you will run

Pick **one** path:

### Path A — Local app + Ollama in Docker (recommended for development)

1. Start Ollama in Docker  
2. Pull Llama 3.2  
3. Run the Spring Boot app with Maven on your machine  
4. Open the UI at `http://localhost:8080`

### Path B — Everything with Docker Compose

1. Build the app  
2. Start app + Ollama with Compose  
3. Pull Llama 3.2 inside the Ollama container if needed  
4. Open the UI at `http://localhost:8080`

### Path C — Ollama installed on the host (no Docker for Ollama)

1. Install Ollama from the official site  
2. Pull Llama 3.2  
3. Run the Spring Boot app with Maven  

---

## 3. Configuration overview (no code)

Main config file: `src/main/resources/application.yml`

Important settings you may change via environment variables:

| Variable              | Default                      | Meaning                          |
|-----------------------|------------------------------|----------------------------------|
| `OLLAMA_BASE_URL`     | `http://localhost:11434`     | Where the app finds Ollama       |
| `OLLAMA_MODEL`        | `llama3.2`                   | Model name                       |
| `OLLAMA_PULL_STRATEGY`| `when_missing`               | Pull model at startup if missing |
| `SERVER_PORT`         | `8080`                       | App HTTP port                    |

Rules:

- App running **on your machine** → use `http://localhost:11434` for Ollama  
- App running **inside Docker Compose** → use `http://ollama:11434` (service name)

Do not hard-code secrets. Use environment variables for any API keys if you later enable cloud speech services.

---

## 4. Path A — Local app + Ollama in Docker

### Step 1: Start Ollama

```bash
docker run -d \
  --name ollama \
  -p 11434:11434 \
  -v ollama_data:/root/.ollama \
  --restart unless-stopped \
  ollama/ollama


docker run -d --name ollama -p 11434:11434 -v ollama_data:/root/.ollama ollama/ollama
docker exec -it ollama ollama pull llama3.2

# Terminal 2 — App
cd /path/to/ai-chat
mvn clean spring-boot:run
