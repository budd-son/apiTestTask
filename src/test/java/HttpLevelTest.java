import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@DisplayName("TC-6 HTTP-уровень")
class HttpLevelTest extends BaseTest {


    @Test
    @DisplayName("TC-6.1 GET /endpoint → 405 Method Not Allowed")
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
    @DisplayName("TC-6.2 Content-Type: application/json → 415 (или 400)")
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
    @DisplayName("TC-6.3 POST /unknown → 404 Not Found")
    void unknownPathReturns404() {
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
    @DisplayName("TC-6.4 Пустое тело → 400 Bad Request")
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
