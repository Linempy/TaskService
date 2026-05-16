# Сервис очереди задач на обработку файлов

Kotlin / Spring Boot 3 приложение: приём задач на обработку файлов (метаданные / ключ в объектном хранилище), очередь задач, асинхронная обработка, история статусов и метрики времени выполнения.

## Стек

- Kotlin, Spring Boot 3.2, Spring Data JPA  
- PostgreSQL, Liquibase  
- MinIO (S3-совместимое API)  
- OpenAPI / Swagger UI (`springdoc`)  
- Micrometer + Actuator + Prometheus  
- Тесты: JUnit 5, Testcontainers (PostgreSQL + MinIO)

## Как работает обработка после создания задачи

**`POST /api/v1/tasks`** создаёт запись со статусом `PENDING` и добавляет строку в историю (`null → PENDING`). Отдельный вызов «начать обработку» для новой задачи **не обязателен**: фоновый **планировщик** (`TaskSchedulerService`) с интервалом из `app.task.poll-interval` подбирает задачи в статусе `PENDING` и вызывает асинхронный **`TaskProcessor`** (пул `asyncTaskExecutor`). Лимит одновременных «рабочих» задач задаётся через **`thread-pool.async.task.max-pool-size`** и счётчик строк со статусом `PROCESSING`.

**`POST /api/v1/tasks/{id}/process`** нужен для **повторного запуска** после терминальных статусов **`FAILED`** или **`CANCELLED`**: переводит задачу обратно в `PENDING`, сбрасывает флаг отмены и результат и снова ставит в очередь (ручной retry).

Таким образом, отдельный эндпоинт «запуска обработки» можно трактовать как **явный retry**; 

## API (кратко)

| Метод | Путь | Описание |
|--------|------|-----------|
| POST | `/api/v1/tasks` | Создать задачу (`201`) |
| GET | `/api/v1/tasks/{id}` | Задача по id |
| GET | `/api/v1/tasks/{id}/history` | **История смены статусов** (хронологически) |
| GET | `/api/v1/tasks` | Список с фильтрами и пагинацией |
| POST | `/api/v1/tasks/{id}/process` | Retry: `FAILED` / `CANCELLED` → `PENDING` (`202`) |
| DELETE | `/api/v1/tasks/{id}` | Отмена (`PENDING` / `PROCESSING`) (`202`) |

Фильтры списка (query): `status`, `taskType`, `createdFrom`, `createdTo` (ISO-8601), плюс стандартные `page`, `size`, `sort`.

Swagger UI: после запуска приложения - `/swagger-ui.html` (или путь из springdoc по умолчанию).

## История статусов

Таблица `task_status_history` (Liquibase): каждый переход фиксируется с полями `old_status`, `new_status`, `changed_at`, `changed_by` (`API` — действия через REST, `WORKER` — воркер при смене статуса в процессе обработки). Опционально `metadata` (JSONB) для расширений.

## Метрики

- Таймер **`tasks.processing.duration`** - время от перевода в `PROCESSING` до завершения (успех / ошибка / отмена), теги:  
  - `task_type` - `REPORT_GENERATION`, `IMAGE_PROCESSING`, `DATA_EXPORT` или `UNKNOWN`  
  - `outcome` - `success`, `failed`, `cancelled`

Просмотр:

- JSON: `GET /actuator/metrics/tasks.processing.duration`  
- Prometheus: `GET /actuator/prometheus`

## Запуск локально

1. Поднять инфраструктуру:

### С использованием Makefile (рекомендуется)

```bash
# Старт приложения
make up
```
### Или без makefile:

   ```bash
   docker compose up -d --build
   ```

   В `docker-compose.yaml` бакет `testbacket` создаётся сервисом `createbuckets` и совпадает с `services.s3.bucketName` в `application.yaml`.


## Тесты

Интеграционные тесты используют Testcontainers (нужен Docker):

```bash
./gradlew test --tests "com.manticore.integration.TaskProcessingFlowTest"
./gradlew test --tests "com.manticore.integration.TaskApiIntegrationTest"
```

Профиль `test` и динамические свойства задаются в `BaseIntegrationTest`.

## Структура пакетов

- `controller` - REST  
- `service` - бизнес-логика, `TaskProcessor`, транзакции обработки, запись истории и метрик  
- `scheduler` - опрос очереди со статусом `PENDING`  
- `repository`, `model`, `dto`, `mapper`, `config`, `exception`

## Makefile команды
```bash
Команда	Описание
make up	    Старт всех сервисов в Docker
make down	Остановка всех сервисов
make restart	Перезапуск сервисов
make build	Сборка JAR (без тестов)
make test	Запуск тестов
make swagger	Показать Swagger UI URL
make help	Справка
```