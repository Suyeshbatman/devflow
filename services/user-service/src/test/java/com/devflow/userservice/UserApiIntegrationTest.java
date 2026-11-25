package com.devflow.userservice;

// JsonPath is a CLASS for reading values out of JSON text with paths like "$.accessToken".
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
// @Autowired is an ANNOTATION: inject a bean into a FIELD. (Test classes are created by JUnit,
//   not Spring, so constructor injection isn't the default here. Field injection is the norm in tests.)
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
// @AutoConfigureMockMvc is an ANNOTATION (in its Spring Boot 4 package): set up MockMvc/MockMvcTester.
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
// HttpStatus is an ENUM of status codes (CREATED = 201, CONFLICT = 409...).
import org.springframework.http.HttpStatus;
// MediaType is a CLASS; MediaType.APPLICATION_JSON is a CONSTANT ("application/json").
import org.springframework.http.MediaType;
// MockMvcTester is a CLASS: sends simulated HTTP requests and checks responses AssertJ-style.
import org.springframework.test.web.servlet.assertj.MockMvcTester;
// MvcTestResult is an INTERFACE: the result of one simulated request.
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
// Provide a MockMvcTester bean that sends requests through the full filter chain.
@AutoConfigureMockMvc
// Use the Testcontainers PostgreSQL.
@Import(TestcontainersConfiguration.class)
public class UserApiIntegrationTest {

    private static final String PASSWORD = "SuperSecret123";
    @Autowired
    private MockMvcTester mvc;

    // ===================== Registration =====================
    @Test
    // Successful registration: 201, a Location header, the right fields, and no password.
    void registerReturns201WithUserAndWithoutPassword() {
        // [LOCAL VARIABLE] A unique email, so this test never clashes with other tests' users.
        String email = uniqueEmail();

        // [LOCAL VARIABLE] result. [METHOD CALL] register(...) is our helper below; it sends the request.
        MvcTestResult result = register(email);

        // [STATIC METHOD CALL] assertThat(result) + [METHOD CALL] .hasStatus(...) + .containsHeader(...)
        assertThat(result).hasStatus(HttpStatus.CREATED).containsHeader("Location");
        // [METHOD CALL] .bodyJson() parses the response body as JSON;
        //   .extractingPath("$.email") reads one value ("$" = the root of the JSON).
        assertThat(result).bodyJson().extractingPath("$.email").isEqualTo(email);
        assertThat(result).bodyJson().extractingPath("$.role").isEqualTo("DEVELOPER");
        // [METHOD CALL] .doesNotHavePath(...): the security check that a hash NEVER leaks out.
        assertThat(result).bodyJson().doesNotHavePath("$.passwordHash");
    }
    @Test
        // [METHOD] Emails are normalized: trimmed and lowercased.
    void registerNormalizesEmail() {
        String email = uniqueEmail();

        // [METHOD CALL] .toUpperCase() on the String OBJECT, plus spaces around it.
        MvcTestResult result = register("  " + email.toUpperCase() + "  ");

        assertThat(result).hasStatus(HttpStatus.CREATED);
        assertThat(result).bodyJson().extractingPath("$.email").isEqualTo(email);
    }

    @Test
        // [METHOD] Registering the same email twice → 409.
    void registerDuplicateEmailReturns409() {
        String email = uniqueEmail();
        register(email); // first time: succeeds

        // [LOCAL VARIABLE] second: the duplicate attempt.
        MvcTestResult second = register(email);

        assertThat(second).hasStatus(HttpStatus.CONFLICT);
        assertThat(second).bodyJson().extractingPath("$.title").isEqualTo("Email already registered");
    }

    @Test
        // [METHOD] Invalid input → 400, with an error listed for each bad field.
    void registerWithInvalidDataReturns400WithFieldErrors() {
        // [LOCAL VARIABLE] A Java "text block" ("""...""") is a multi-line String. Handy for JSON.
        String invalidJson = """
                {"email": "not-an-email", "password": "short", "fullName": ""}
                """;

        // [METHOD CALL] chain on the mvc OBJECT: .post() → .uri(...) → .contentType(...) → .content(...)
        //   builds the request; .exchange() SENDS it and returns the result.
        MvcTestResult result = mvc.post().uri("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson)
                .exchange();

        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
        // [METHOD CALL] .hasPath(...): this JSON path must exist.
        assertThat(result).bodyJson().hasPath("$.errors.email");
        assertThat(result).bodyJson().hasPath("$.errors.password");
        assertThat(result).bodyJson().hasPath("$.errors.fullName");
    }

    // ===================== Login & tokens =====================

    @Test
        // [METHOD] The full flow: register → log in → use the token on /me.
        //   "throws Exception": our loginAndGetToken helper can throw a checked exception.
    void loginThenGetMeReturnsOwnProfile() throws Exception {
        String email = uniqueEmail();
        register(email);

        // [LOCAL VARIABLE] token: a REAL JWT, issued by the real TokenService.
        String token = loginAndGetToken(email);

        // [METHOD CALL] .header(name, value) adds the Authorization header, exactly like curl's -H.
        MvcTestResult result = mvc.get().uri("/api/users/me")
                .header("Authorization", "Bearer " + token)
                .exchange();

        // [METHOD CALL] .hasStatusOk() = hasStatus(HttpStatus.OK)
        assertThat(result).hasStatusOk();
        assertThat(result).bodyJson().extractingPath("$.email").isEqualTo(email);
        // The login we just did must have been recorded (this checks the V2 migration plus dirty checking).
        assertThat(result).bodyJson().hasPath("$.lastLoginAt");
    }

    @Test
        // [METHOD] Wrong password → 401.
    void loginWithWrongPasswordReturns401() {
        String email = uniqueEmail();
        register(email);

        MvcTestResult result = mvc.post().uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                // [METHOD CALL] loginJson(...) is our helper that builds the JSON body.
                .content(loginJson(email, "WrongPassword999"))
                .exchange();

        assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
    }

    // ===================== Security rules =====================

    @Test
        // [METHOD] No token → 401 (blocked by the security filter chain before any controller runs).
    void meWithoutTokenReturns401() {
        assertThat(mvc.get().uri("/api/users/me").exchange()).hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
        // [METHOD] A DEVELOPER can't use the ADMIN-only endpoint → 403.
    void developerCannotLookUpUsersById() throws Exception {
        String email = uniqueEmail();

        // [METHOD CALL] .getResponse().getContentAsString(): the raw JSON text of the registration response.
        String registerBody = register(email).getResponse().getContentAsString();
        // [STATIC METHOD CALL] JsonPath.read(json, path): pull the new user's id out of that JSON.
        String userId = JsonPath.read(registerBody, "$.id");
        String token = loginAndGetToken(email);

        MvcTestResult result = mvc.get().uri("/api/users/" + userId)
                .header("Authorization", "Bearer " + token)
                .exchange();

        // Authenticated (valid token) but NOT authorized (wrong role) → 403, not 401.
        assertThat(result).hasStatus(HttpStatus.FORBIDDEN);
    }

    // ===================== Helper methods =====================
    // [METHOD] "private" helpers: not tests themselves (no @Test). They remove repetition from the tests above.

    // [METHOD] Sends a registration request for this email and returns the result.
    private MvcTestResult register(String email) {
        return mvc.post().uri("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                // [METHOD CALL] .formatted(...) on a String: fills each %s placeholder in order.
                .content("""
                        {"email": "%s", "password": "%s", "fullName": "Test User"}
                        """.formatted(email, PASSWORD))
                .exchange();
    }

    // [METHOD] Logs in and returns just the access token.
    //   "throws Exception": getContentAsString() can throw a CHECKED exception.
    private String loginAndGetToken(String email) throws Exception {
        MvcTestResult result = mvc.post().uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson(email, PASSWORD))
                .exchange();

        // Fail early with a clear message if the login itself didn't work.
        assertThat(result).hasStatusOk();
        // [STATIC METHOD CALL] JsonPath.read(...) returns the "accessToken" value as a String.
        return JsonPath.read(result.getResponse().getContentAsString(), "$.accessToken");
    }

    // [STATIC METHOD] Builds a login JSON body. "static" because it uses no FIELDS of the test object.
    private static String loginJson(String email, String password) {
        return """
                {"email": "%s", "password": "%s"}
                """.formatted(email, password);
    }

    // [STATIC METHOD] A new random email each time, e.g. "user-3f2a9c1e-...@example.com".
    //   All the tests share ONE database, so unique emails stop them from interfering with each other.
    private static String uniqueEmail() {
        // [STATIC METHOD CALL] UUID.randomUUID() returns a random UUID OBJECT; "+" turns it into text.
        return "user-" + UUID.randomUUID() + "@example.com";
    }

}
