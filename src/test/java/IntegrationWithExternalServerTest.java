import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;


@Epic("API тестирование")
@Feature("Интеграция с внешним сервисом")
public class IntegrationWithExternalServerTest extends BaseTest {
    @Test
    @Story("LOGIN → /auth")
    @DisplayName("INT-1 LOGIN → 200, /auth вызван ровно 1 раз")
    @Description("Успешный LOGIN триггерит ровно один POST на /auth внешнего сервиса.")
    public void succesLoginWithExternalServer() {
        given()
                .body(form(token,"LOGIN"))
                .when()
                .post()
                .then()
                .statusCode(200)
                .log().ifValidationFails();
        verify(1, postRequestedFor(urlEqualTo("/auth")));
    }

    @Test
    @Story("LOGIN → /auth")
    @DisplayName("INT-2 Тело запроса к /auth = token=<token>")
    @Description("Приложение передаёт во внешний сервис ровно 'token=<token>' без лишних полей.")
    public void correctBodyRequestWithExternalServer() {
        given()
                .body(form(token,"LOGIN"))
                .when()
                .post()
                .then()
                .statusCode(200)
                .log().ifValidationFails();
        verify(postRequestedFor(urlEqualTo("/auth")).withRequestBody(equalTo("token=" + token)));
    }
    @Test
    @Story("Невалидный токен не ходит в /auth")
    @DisplayName("INT-3 Невалидный токен → 400, /auth не вызван")
    @Description("Валидация токена выполняется до похода в /auth: невалидный токен не должен " +
            "вызывать внешний сервис.")
    public void incorrectTokenDoesNotCallExternalServer() {
        String incorrectToken  = token + "1" ;
        given()
                .body(form(incorrectToken,"LOGIN"))
                .when()
                .post()
                .then()
                .statusCode(400)
                .log().ifValidationFails();
        verify(0, postRequestedFor(urlEqualTo("/auth")));
    }

    @Test
    @Story("ACTION без LOGIN не ходит в /doAction")
    @DisplayName("INT-4 ACTION без LOGIN → 403, /doAction не вызван")
    @Description("Без активной сессии приложение отклоняет ACTION до похода в /doAction.")    public void ActionWithoutLoginDoesNotCallExternalServer() {
        given()
                .body(form(token,"ACTION"))
                .when()
                .post()
                .then()
                .statusCode(403)
                .log().ifValidationFails();
        verify(0, postRequestedFor(urlEqualTo("/doAction")));
    }

    @Test
    @Story("LOGOUT не ходит во внешний сервис")
    @DisplayName("INT-5 LOGOUT → 200, /auth и /doAction не вызваны после LOGIN")
    @Description("LOGOUT обрабатывается локально и не триггерит внешние вызовы.")
    public void logoutDontCallExternalServer() {
        given()
                .body(form(token,"LOGIN"))
                .when()
                .post()
                .then()
                .log()
                .ifValidationFails()
                .statusCode(200);
        wireMock.resetRequests();
        given()
                .body(form(token,"LOGOUT"))
                .when()
                .post()
                .then()
                .statusCode(200)
                .log().ifValidationFails();
        verify(0, postRequestedFor(urlEqualTo("/auth")));
        verify(0, postRequestedFor(urlEqualTo("/doAction")));
    }

    @ParameterizedTest(name = "TC-4.6 /auth={0} → приложению 500, токен не сохранён")
    @ValueSource(ints = {
            400,
            401,
            403,
            404,
            500,
            503})
    @Story("Ошибка /auth")
    @DisplayName("INT-6 Ошибка /auth (400/401/403/404/500/503) → 500, токен не сохранён")
    @Description("Любая ошибка /auth приводит к 500 приложению. Токен не сохраняется: " +
            "повторный ACTION возвращает 403.")
    void authErrorReturns500AndTokenNotSaved(int externalStatus) {
        stubFor(post(urlEqualTo("/auth"))
                .willReturn(aResponse().withStatus(externalStatus)));

        given().body(form(token, "LOGIN")).when().post().then()
                .log().ifValidationFails()
                .statusCode(500);

        given().body(form(token, "ACTION")).when().post().then()
                .log().ifValidationFails()
                .statusCode(403);
    }

    @Test
    @Story("Ошибка /doAction")
    @DisplayName("INT-7 /doAction=500 → 500, сессия жива (повторный ACTION=200)")
    @Description("Ошибка внешнего /doAction даёт 500 приложению. Сессия сохраняется: " +
            "после восстановления /doAction повторный ACTION возвращает 200.")
    public void actionWithExternalServerActionError() {

        given().body(form(token, "LOGIN")).when().post().then()
                .log().ifValidationFails()
                .statusCode(200);
        stubFor(post(urlEqualTo("/doAction"))
                .willReturn(aResponse().withStatus(500)));
        given().body(form(token, "ACTION")).when().post().then().log().ifValidationFails().statusCode(500);
        stubFor(post(urlEqualTo("/doAction")).willReturn(ok()));

        given().body(form(token, "ACTION")).when().post().then()
                .log().ifValidationFails()
                .statusCode(200);
        /* или сессия сброшена
        given().body(requestAction).when().post().then()
        .log().ifValidationFails()
        .statusCode(403);
         */
    }
}