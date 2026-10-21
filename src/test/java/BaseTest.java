import com.github.tomakehurst.wiremock.WireMockServer;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;


public abstract class BaseTest {
    protected static WireMockServer wireMock;
    protected static final String BASE_URL = "http://localhost:8080";
    protected static final String API_KEY = "qazWSXedc";
    protected String token;
    protected static RequestSpecification spec;

    @BeforeAll
    static void globalSetUp() {
        wireMock = new WireMockServer(8888);
        wireMock.start();
        configureFor("localhost", 8888);
        stubFor(post(urlEqualTo("/auth")).willReturn(aResponse().withStatus(200)));
        stubFor(post(urlEqualTo("/doAction")).willReturn(aResponse().withStatus(200)));
        RestAssured.filters(new AllureRestAssured());
        RestAssured.requestSpecification = new RequestSpecBuilder()
                .setBaseUri(BASE_URL)
                .setBasePath("/endpoint")
                .addHeader("X-Api-Key", API_KEY)
                .addHeader("Accept", "application/json")
                .setContentType("application/x-www-form-urlencoded")
                .build();
    }


    @BeforeEach
    void setup() {
        token = newToken();
        wireMock.resetAll(); // Очищаем всё

        // Настраиваем моки заново перед КАЖДЫМ тестом
        stubFor(post(urlEqualTo("/auth")).willReturn(aResponse().withStatus(200)));
        stubFor(post(urlEqualTo("/doAction")).willReturn(aResponse().withStatus(200)));
    }

    @AfterEach
    void tearDown() {
        try {
            given()
                    .body("token=" + token + "&action=LOGOUT")
                    .when()
                    .post();
        } catch (Exception ignored) {}
    }

    protected static String newToken() {
        return UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }


    @AfterAll
    static void globalTearDown() {
        wireMock.stop();
    }
    protected static String form(String token, String action) {
        return "token=" + URLEncoder.encode(token, StandardCharsets.UTF_8)
                + "&action=" + URLEncoder.encode(action, StandardCharsets.UTF_8);
    }
}
