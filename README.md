# API Test Task — автотесты для веб-сервиса

Репозиторий с автотестами для тестового задания на вакансию AQA Engineer.

## Стек

- Java 17+
- JUnit 5
- RestAssured 5.3.2
- WireMock 3.3.1
- Allure 2.24.0
- Maven

## Структура проекта

```
src/test/java/
├── BaseTest.java                          # общая инфраструктура: WireMock, RestAssured, токены
├── AuthorizationTest.java                 # проверки X-Api-Key
├── ValidationTokenTest.java               # валидация token
├── ValidationActionTest.java              # валидация action
├── BusinessLogicTest.java                 # state machine: LOGIN → ACTION → LOGOUT
├── IntegrationWithExternalServerTest.java # интеграция с /auth и /doAction (WireMock)
└── HttpLevelTest.java                     # HTTP-уровень: методы, Content-Type, пути
```

## Требования

- Java 17 или выше
- Maven 3.8+
- Свободные порты: `8080` (приложение), `8888` (WireMock)

## Запуск приложения

Тестируемое приложение — Spring Boot. Запускается отдельно, **до** тестов:

```bash
java -jar -Dsecret=qazWSXedc -Dmock=http://localhost:8888/ internal-0.0.1-SNAPSHOT.jar
```

- `-Dsecret=qazWSXedc` — статический `X-Api-Key`, который принимает приложение.
- `-Dmock=http://localhost:8888/` — адрес внешнего сервиса, заменённого на WireMock.

Без запущенного приложения все тесты упадут с `Connection refused` или `401`.

## Запуск тестов

```bash
# все тесты
mvn clean test

# только конкретный класс
mvn test -Dtest=BusinessLogicTest

# только конкретный тест
mvn test -Dtest=BusinessLogicTest#repeatLoginFail

# с генерацией Allure-отчёта и открытием в браузере
mvn clean test allure:serve
```

## Allure-отчёт

После прогона результаты лежат в `target/allure-results/`. Сгенерировать и открыть:

```bash
mvn allure:serve
```

Отчёт откроется в браузере. Для нетехнического читателя — вкладка **Behaviors**
(тесты сгруппированы по `@Epic` / `@Feature` / `@Story`). Для разработчика —
вкладка **Suites** (по классам).

## Что тестируется

Подробное описание тест-кейсов, ожидаемых результатов и известных багов —
в отдельном файле **[TEST_CASE.md](TEST_CASE.md)**.

Кратко:
- `AuthorizationTest` — `X-Api-Key` (валидный, неверный, отсутствует).
- `ValidationTokenTest` — формат `token` (32 символа `A-Z0-9`).
- `ValidationActionTest` — допустимые `action` (`LOGIN`, `ACTION`, `LOGOUT`).
- `BusinessLogicTest` — переходы состояний: LOGIN → ACTION → LOGOUT.
- `IntegrationWithExternalServerTest` — ходит ли приложение в `/auth` и `/doAction`.
- `HttpLevelTest` — нестандартные HTTP-запросы (метод, Content-Type, путь, тело).



