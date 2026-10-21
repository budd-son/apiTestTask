import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.core.IsEqual.equalTo;

@Epic("API тестирование")
@Feature("Бизнес-логика")
public class BusinessLogicTest extends BaseTest {
    @Test
    @Story("Многократный ACTION")
    @DisplayName("BIZ-9 LOGIN → ACTION ×3 → все 200, result=OK")
    @Description("После одного LOGIN можно выполнить ACTION несколько раз. Все вызовы — 200.")
    public void sucManyActionAfterLogin() {
        given()
                .body(form(token, "LOGIN"))
                .when()
                .post()
                .then()
                .statusCode(200)
                .log().ifValidationFails();
        for(int i = 0; i < 3; i++){
            given()
                    .body(form(token, "ACTION"))
                    .when()
                    .post()
                    .then()
                    .statusCode(200)
                    .body("result", equalTo("OK"));
        }
    }
    @Test
    @Story("Цикл LOGIN → LOGOUT")
    @DisplayName("BIZ-10 Цикл LOGIN → LOGOUT ×3 → каждый шаг 200")
    @Description("После LOGOUT токен удаляется, повторный LOGIN тем же токеном должен работать. " +
            "Цикл из 3 итераций проходит без ошибок.")    public void cycleLoginLogout() {
        for(int i = 0; i < 3; i++){
            given()
                    .body(form(token, "LOGIN"))
                    .when()
                    .post()
                    .then()
                    .statusCode(200)
                    .body("result", equalTo("OK"));
            given()
                    .body(form(token, "LOGOUT"))
                    .when()
                    .post()
                    .then()
                    .statusCode(200)
                    .body("result", equalTo("OK"));
        }
    }
    @Test
    @Story("Повторный LOGIN")
    @DisplayName("BIZ-6 Повторный LOGIN тем же токеном → 409, message='...already exists'")
    @Description("Повторный LOGIN с тем же токеном не создаёт вторую сессию: " +
            "возвращается 409 Conflict с сообщением о существующем токене.")
    public void repeatLoginFail() {
        String expectedMessage = "Token '" + token + "' already exists";
        given().body(form(token, "LOGIN"))
                .when()
                .post()
                .then()
                .statusCode(200);
        given().body(form(token, "LOGIN"))
                .when()
                .post()
                .then()
                .statusCode(409)
                .body("message", equalTo(expectedMessage));
    }

    @Test
    @Story("Двойной LOGOUT")
    @DisplayName("BIZ-8 LOGOUT ×2 → второй 403, message='...not found'")
    @Description("Повторный LOGOUT несуществующей сессии возвращает 403 с сообщением 'not found'.")
    public void repeatLogoutFail() {
        String expectedMessage = "Token '" + token + "' not found";

        given()
                .body(form(token, "LOGIN"))
                .when()
                .post()
                .then()
                .statusCode(200);
        given()
                .body(form(token, "LOGOUT"))
                .when()
                .post()
                .then()
                .statusCode(200);
        given()
                .body(form(token, "LOGOUT"))
                .when()
                .post()
                .then()
                .statusCode(403)
                .body("message", equalTo(expectedMessage));

    }
    @Test
    @Story("LOGOUT без LOGIN")
    @DisplayName("BIZ-4 LOGOUT без LOGIN → 403, message='...not found'")
    @Description("LOGOUT без предварительного LOGIN возвращает 403: сессии нет.")
    public void LogoutWithoutLoginFail() {
        String expectedMessage = "Token '" + token + "' not found";
        given()
                .body(form(token, "LOGOUT"))
                .when()
                .post()
                .then()
                .statusCode(403)
                .body("message", equalTo(expectedMessage));
    }
    @Test
    @Story("ACTION без LOGIN")
    @DisplayName("BIZ-2 ACTION без LOGIN → 403, message='...not found'")
    @Description("ACTION без предварительного LOGIN возвращает 403: сессии нет.")
    public void ActionWithoutLoginFail() {
        String expectedMessage = "Token '" + token + "' not found";
        given()
                .body(form(token, "ACTION"))
                .when()
                .post()
                .then()
                .statusCode(403)
                .body("message", equalTo(expectedMessage));
    }

    @Test
    @Story("Очистка сессии")
    @DisplayName("BIZ-5 LOGIN → ACTION → LOGOUT → ACTION = 403")
    @Description("После LOGOUT сессия очищается: следующий ACTION возвращает 403.")
    public void clearSessionAfterLogout() {
        given()
                .body(form(token, "LOGIN"))
                .when()
                .post()
                .then()
                .statusCode(200);
        given()
                .body(form(token, "ACTION"))
                .when()
                .post()
                .then()
                .statusCode(200);
        given()
                .body(form(token, "LOGOUT"))
                .when()
                .post()
                .then()
                .statusCode(200);
        given()
                .body(form(token, "ACTION"))
                .when()
                .post()
                .then()
                .statusCode(403);
    }
}

