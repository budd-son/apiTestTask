import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Epic("API тестирование")
@Feature("HTTP-уровень")
@DisplayName("TC-6 HTTP-уровень")
class HttpLevelTest extends BaseTest {


    @Test
    @Story("Неверный метод")
    @DisplayName("HTTP-1 GET /endpoint → 405 Method Not Allowed")
    @Description("GET на POST-эндпоинт должен возвращать 405. Сейчас приложение возвращает 500 " )
    void wrongMethodReturns405() {
        given()
                .body(form(token, "LOGIN"))
                .when()
                .get()
                .then()
                .log().ifValidationFails()
                .statusCode(405);
    }


    @Test
    @Story("Неверный Content-Type")
    @DisplayName("HTTP-2 Content-Type: application/json → 415 (или 400)")
    @Description("Неверный Content-Type должен давать 415/400, а не 500. " +
            "Сейчас приложение возвращает 500 .")
    void wrongContentTypeReturns415or400() {
        given()
                .contentType("application/json")
                .body("{\"token\":\"" + token + "\",\"action\":\"LOGIN\"}")
                .when()
                .post()
                .then()
                .log().ifValidationFails()
                .statusCode(anyOf(is(415), is(400)));
    }

    @Test
    @Story("Несуществующий путь")
    @DisplayName("HTTP-3 POST /unknown → 404 Not Found")
    @Description("Запрос на несуществующий путь должен возвращать 404, а не 500. " +
            "Сейчас приложение возвращает 500.") void unknownPathReturns404() {
        given()
                .basePath("/unknown")
                .body(form(token, "LOGIN"))
                .when()
                .post()
                .then()
                .log().ifValidationFails()
                .statusCode(404);
    }


    @Test
    @Story("Пустое тело")
    @DisplayName("HTTP-4 Пустое тело → 400 Bad Request")
    @Description("Пустое тело запроса должно давать 400: обязательные поля token и action отсутствуют.")
    void emptyBodyReturns400() {
        given()
                .body("")
                .when()
                .post()
                .then()
                .log().ifValidationFails()
                .statusCode(400);
    }

}
