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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@Epic("API тестирование")
@Feature("Валидация токена")
public class ValidationTokenTest extends BaseTest{
    private static final String ERROR_MSG = "token: должно соответствовать \"^[0-9A-F]{32}$\"";
    @Test
    @Story("Валидный токен")
    @DisplayName("TOKEN-1 Валидный токен (32, A-Z0-9) → 200, result=OK")
    @Description("Токен из 32 символов A-Z0-9 проходит валидацию. LOGIN возвращает 200 и result=OK.")
    public void testValidToken(){

        Response response = given()
                .body(form(token, "LOGIN"))
                .when()
                .post()
                .then()
                .statusCode(200)
                .extract()
                .response();
       String responseBody = response.path("result");
        assertNotNull(responseBody);
        assertEquals("OK", responseBody);
    }
    @ParameterizedTest(name = "Невалидный токен: {0}") // {0} подставит значение в название теста
    @ValueSource(strings = {
            "A1B2C3D4E5F6A1B2C3D4E5F6A1B2C3D42", // Слишком длинный
            "A1B2C3D4E5F6A1B2C3D4E5F6A1B2C32",
            "",
            "A1B2C3D4E5F6A1B2C3D4E5F6A1B2C3D ",
            " A1B2C3D4E5F6A1B2C3D4E5F6A1B2C3D",
            "A1B2C3D4E5Ф6A1B2C3D4E5F6A1B2C33D",
            "A1B2C3D4E5F6A1B2C3D4E5F6A1B2C3DG", // Буква G
            "a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4", // Нижний регистр
            "A1B2C3D4E5F6A1B2C3D4E5F6A1B2C3D_"  // Спецсимвол
    })
    @Story("Невалидный формат токена")
    @DisplayName("TOKEN-2..10 Невалидный формат токена → 400")
    @Description("Проверяем длину (31/33), пустую строку, пробелы, кириллицу, не-hex символ, " +
            "нижний регистр и спецсимвол. Все должны дать 400 с сообщением о формате.")
    void testInvalidToken(String invalidToken){
        Response response = given()
                .body(form(invalidToken, "LOGIN"))
                .when()
                .post()
                .then()
                .log().ifValidationFails()
                .statusCode(400)
                .extract()
                .response();
        String result = response.path("result");
        String message = response.path("message");

        assertEquals("ERROR", result);
        assertNotNull(message);
        assertEquals(ERROR_MSG, message);
    }
    @Test
    @Story("Токен отсутствует")
    @DisplayName("TOKEN-11 Токен отсутствует → 400, message='token: не должно равняться null'")
    @Description("Без параметра token приложение возвращает 400 с сообщением о том, что токен обязателен.")
    public void missingToken(){
        String requestBody = "action=LOGIN";
        Response response = given()
                .body(requestBody)
                .when()
                .post()
                .then()
                .statusCode(400)
                .extract()
                .response();
        String result = response.path("result");
        String message = response.path("message");

        assertEquals("ERROR", result);
        assertNotNull(message);
        assertEquals("token: не должно равняться null", message);
    }


}

