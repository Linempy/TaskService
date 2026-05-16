.PHONY: up down build test seed clean logs restart status psql swagger help

DOCKER_COMPOSE = docker-compose
GRADLE = gradlew.bat

DB_USER ?= user
DB_NAME ?= booking_db
API_URL ?= http://localhost:8080


up:
	$(DOCKER_COMPOSE) up -d --build
	@echo "Приложение запущено на http://localhost:8080"
	@echo "Swagger UI: http://localhost:8080/swagger-ui.html"

down:
	$(DOCKER_COMPOSE) down
	@echo "Приложение остановлено"

restart: down up

build:
	$(GRADLE) clean build -x test

clean:
	$(DOCKER_COMPOSE) down -v
	$(GRADLE) clean
	@echo "Очистка выполнена"

logs:
	$(DOCKER_COMPOSE) logs -f

status:
	$(DOCKER_COMPOSE) ps

psql:
	$(DOCKER_COMPOSE) exec postgres psql -U ${DB_USER:-booking_user} -d ${DB_NAME:-booking_db}

swagger:
	@echo "Swagger UI доступно на: http://localhost:8080/swagger-ui.html"
	@echo "OpenAPI spec доступно на: http://localhost:8080/v3/api-docs"

help:
	@echo "Доступные команды:"
	@echo "  make up       - Старт приложения"
	@echo "  make down     - Остановка приложения"
	@echo "  make restart  - Перезапуск приложения"
	@echo "  make build    - Собрать проект"
	@echo "  make test     - Запустить тесты"
	@echo "  make clean    - Очистка"
	@echo "  make logs     - Посмотреть логи приложения"
	@echo "  make status   - Проверить статус контейнера"
	@echo "  make psql     - Подключиться к PostgreSQL"
	@echo "  make swagger  - Показать Swagger UI URL"
