import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@Epic("API тестирование")
@Feature("Валидация action")
public class ValidationActionTest extends BaseTest {
    @Test
    @Story("Валидные действия")
    @DisplayName("ACT-1 LOGIN валиден → 200, result=OK")
    @Description("LOGIN — допустимое действие, возвращает 200 и result=OK.")
    public void validLoginAction() {
         given()
                .body(form(token, "LOGIN"))
                .when()
                .post()
                .then()
                .statusCode(200)
                 .body("result", equalTo("OK"));
    }
    @Test
    @Story("Валидные действия")
    @DisplayName("ACT-2 ACTION после LOGIN → 200, result=OK")
    @Description("ACTION доступен только после LOGIN. После LOGIN возвращает 200 и result=OK.")
    public void validActionAction() {
        given()
                .body(form(token, "LOGIN"))
                .when()
                .post();
        given()
                .body(form(token, "ACTION"))
                .when()
                .post()
                .then()
                .statusCode(200)
                .body("result", equalTo("OK"));
    }

    @Test
    @Story("Валидные действия")
    @DisplayName("ACT-3 LOGOUT после LOGIN → 200, result=OK")
    @Description("LOGOUT завершает сессию. После LOGIN возвращает 200 и result=OK.")
    public void validLogoutAction() {

        given()
                .body(form(token, "LOGIN"))
                .when()
                .post().then().log().all();
        given()
                .body(form(token, "LOGOUT"))
                .when()
                .post()
                .then()
                .log().all()
                .statusCode(200)
                .body("result", equalTo("OK"));

    }
    @Test
    @Story("Невалидные действия")
    @DisplayName("ACT-4 action отсутствует → 400")
    @Description("Без параметра action приложение возвращает 400 с перечнем допустимых значений.")
    public void missingAction() {
        String requestBody = "token=" + token;
        given()
                .body(requestBody)
                .when()
                .post()
                .then()
                .statusCode(400)
                .body("message", equalTo("action: invalid action 'null'. Allowed: LOGIN, LOGOUT, ACTION"));

    }

    @ParameterizedTest(name = "Invalid Action : {0}")
    @ValueSource(strings = {
            "login",
            "NOTHING",
            "",
            " LOGIN",
            "LOGIN "
    })
    @Story("Невалидные действия")
    @DisplayName("ACT-5..9 Невалидный action → 400")
    @Description("Проверяем нижний регистр, неизвестное действие, пустую строку и пробелы. " +
            "Все должны дать 400, но 8,9 дают 200")
    void invalidAction(String action) {
        Response response = given()
                .body((form(token, action)))
                .when()
                .post()
                .then()
                .statusCode(400)
                .log()
                .all()
                .extract()
                .response();
        String result = response.path("result");
        String message = response.path("message");

        assertEquals("ERROR", result);
        assertNotNull(message);
        String expectedActionInError;
        if (action == null || action.isEmpty()) {
            expectedActionInError = "null";
        } else {
            expectedActionInError = action;
        }

        String expectedMessage = String.format("action: invalid action '%s'. Allowed: LOGIN, LOGOUT, ACTION", expectedActionInError);
        assertEquals("ERROR", result);
        assertEquals(expectedMessage, message);
    }
}

