import com.github.tomakehurst.wiremock.WireMockServer;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@Epic("API тестирование")
@Feature("Авторизация (X-Api-Key)")
public class AuthorizationTest {

    static WireMockServer wireMockServer;
    private static final String BASE_URL = "http://localhost:8080";
    private static final String ERROR_MSG = "Missing or invalid API Key";
    private static final String  VALID_TOKEN = "A1B2C3D4E526A1B2C3D4E5F6A1B2C3D4";
    @BeforeAll
    public static void beforeAll() {
        wireMockServer = new WireMockServer(8888);
        wireMockServer.start();
        configureFor("localhost", 8888);

        stubFor(post(urlEqualTo("/auth")).willReturn(aResponse().withStatus(200)));
        stubFor(post(urlEqualTo("/doAction")).willReturn(aResponse().withStatus(200)));
    }

    @Test
    @Story("Успешная авторизация")
    @DisplayName("AUTH-1 Успешный вход с правильным ключом")
    @Description("Проверяем, что с валидным X-Api-Key приложение принимает LOGIN и возвращает 200 OK.")
    public void successfulAuthenticationTest() {
        String requestBody = "token=" + VALID_TOKEN + "&action=LOGIN";

        given()
                .contentType("application/x-www-form-urlencoded")
                .header("X-Api-Key", "qazWSXedc")
                .header("Accept", "application/json")
                .body(requestBody)
                .when()
                .post(BASE_URL + "/endpoint") // Правильный эндпоинт
                .then()
                .log().all()
                .statusCode(200);
    }



    @ParameterizedTest(name = "API-ключ: {0}")
    @ValueSource(strings = {
            "wrong_key",
            "QAZWSXEDC",
            "qazWSXedc ",
            " qazWSXedc"
    })
    @Story("Неверный X-Api-Key")
    @DisplayName("AUTH-2..5 Неверный ключ → 401 (кроме пробелов — там  500)")
    @Description("Неверный ключ, другой регистр, пробелы в начале/конце. " +
            "Ожидается 401 Unauthorized. Для ключа с пробелом сейчас 500.")
    public void invalidApiKeyTest(String invalidKey) {
        Response response = given()
                .header("X-Api-Key", invalidKey)
                .body("token=" + VALID_TOKEN + "&action=LOGIN")
                .when()
                .post()
                .then()
                .log().all()
                .statusCode(401)
                .extract().response();

            assertEquals("ERROR", response.path("result"));

    }

    @Test
    @Story("Отсутствует X-Api-Key")
    @DisplayName("AUTH-6 Ключ отсутствует → 401, message='Missing or invalid API Key'")
    @Description("Без заголовка X-Api-Key приложение должно вернуть 401 с сообщением об ошибке.")
    public void missingApiKeyTest() {
        String requestBody = "token=" + VALID_TOKEN + "&action=LOGIN";

        Response response = given()
                .contentType("application/x-www-form-urlencoded")
                .header("Accept", "application/json")
                .body(requestBody)
                .when()
                .post(BASE_URL + "/endpoint")
                .then()
                .log().all()
                .statusCode(401).extract().response();
        String result = response.path("result");
        String message = response.path("message");

        assertEquals("ERROR", result);
        assertNotNull(message);
        assertEquals(ERROR_MSG, message);

    }

    @Test
    @Story("Неверный ключ + неверный токен")
    @DisplayName("AUTH-7 Неверный ключ + неверный токен → 401")
    @Description("Проверка ключа выполняется раньше проверки токена: неверный ключ даёт 401, " +
            "даже если токен тоже невалиден.")
    public void invalidApiKeyAndTokenTest() {
        String requestBody = "token=" + VALID_TOKEN + "1" + "&action=LOGIN";
        given()
                .contentType("application/x-www-form-urlencoded")
                .header("Accept", "application/json")
                .header("X-Api-Key", "qazWSedc")
                .body(requestBody)
                .when()
                .post(BASE_URL + "/endpoint")
                .then()
                .statusCode(401)
                .body("message", equalTo(ERROR_MSG));

    }


    @AfterEach
    public void afterEach() {
        given()
                .contentType("application/x-www-form-urlencoded")
                .header("X-Api-Key", "qazWSXedc")
                .body("token=" + VALID_TOKEN + "&action=LOGOUT")
                .post(BASE_URL + "/endpoint");
    }

    @AfterAll
    public static void afterAll() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }

    }

}
