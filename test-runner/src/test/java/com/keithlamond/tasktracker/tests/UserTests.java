package com.keithlamond.tasktracker.tests;

import com.keithlamond.tasktracker.base.BaseTest;
import com.keithlamond.tasktracker.model.UserRequest;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class UserTests extends BaseTest {

    // ─── Create User ─────────────────────────────────────────────────────────

    @Test
    public void createUser_ValidPayload_Returns201WithGeneratedId() {
        UserRequest request = new UserRequest("Test User", "testuser_create@example.com");

        given()
                .spec(requestSpec)
                .body(request)
                .when()
                .post("/users")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("name", equalTo("Test User"))
                .body("email", equalTo("testuser_create@example.com"))
                .body("createdAt", notNullValue());
    }

    @Test
    public void createUser_DuplicateEmail_Returns409() {
        UserRequest request = new UserRequest("Duplicate User", "duplicate@example.com");

        // First request — should succeed
        given()
                .spec(requestSpec)
                .body(request)
                .when()
                .post("/users")
                .then()
                .statusCode(201);

        // Second request with same email — should conflict
        given()
                .spec(requestSpec)
                .body(request)
                .when()
                .post("/users")
                .then()
                .statusCode(409);
    }

    @Test
    public void createUser_MissingName_Returns400() {
        UserRequest request = new UserRequest(null, "missingname@example.com");

        given()
                .spec(requestSpec)
                .body(request)
                .when()
                .post("/users")
                .then()
                .statusCode(400);
    }

    @Test
    public void createUser_MissingEmail_Returns400() {
        UserRequest request = new UserRequest("No Email User", null);

        given()
                .spec(requestSpec)
                .body(request)
                .when()
                .post("/users")
                .then()
                .statusCode(400);
    }

    // ─── Get User ─────────────────────────────────────────────────────────────

    @Test
    public void getUserById_ValidId_Returns200WithCorrectData() {
        // Create a user first
        UserRequest request = new UserRequest("Get User Test", "getuser@example.com");

        Response createResponse = given()
                .spec(requestSpec)
                .body(request)
                .when()
                .post("/users")
                .then()
                .statusCode(201)
                .extract().response();

        Long userId = createResponse.jsonPath().getLong("id");

        // Retrieve the user by ID
        given()
                .spec(requestSpec)
                .when()
                .get("/users/" + userId)
                .then()
                .statusCode(200)
                .body("id", equalTo(userId.intValue()))
                .body("name", equalTo("Get User Test"))
                .body("email", equalTo("getuser@example.com"));
    }

    @Test
    public void getUserById_InvalidId_Returns404() {
        given()
                .spec(requestSpec)
                .when()
                .get("/users/999999")
                .then()
                .statusCode(404);
    }

    // ─── Delete User ──────────────────────────────────────────────────────────

    @Test
    public void deleteUser_ValidId_Returns204() {
        UserRequest request = new UserRequest("Delete Me", "deleteme@example.com");

        Response createResponse = given()
                .spec(requestSpec)
                .body(request)
                .when()
                .post("/users")
                .then()
                .statusCode(201)
                .extract().response();

        Long userId = createResponse.jsonPath().getLong("id");

        given()
                .spec(requestSpec)
                .when()
                .delete("/users/" + userId)
                .then()
                .statusCode(204);
    }

    @Test
    public void deleteUser_InvalidId_Returns404() {
        given()
                .spec(requestSpec)
                .when()
                .delete("/users/999999")
                .then()
                .statusCode(404);
    }

    @Test
    public void deleteUser_CascadesTaskDeletion() {
        // Create a user
        UserRequest userRequest = new UserRequest("Cascade User", "cascade@example.com");

        Response userResponse = given()
                .spec(requestSpec)
                .body(userRequest)
                .when()
                .post("/users")
                .then()
                .statusCode(201)
                .extract().response();

        Long userId = userResponse.jsonPath().getLong("id");

        // Create a task owned by that user
        String taskBody = "{\"title\": \"Cascade Task\", \"userId\": " + userId + "}";

        Response taskResponse = given()
                .spec(requestSpec)
                .body(taskBody)
                .when()
                .post("/tasks")
                .then()
                .statusCode(201)
                .extract().response();

        Long taskId = taskResponse.jsonPath().getLong("id");

        // Delete the user
        given()
                .spec(requestSpec)
                .when()
                .delete("/users/" + userId)
                .then()
                .statusCode(204);

        // Verify the task is gone
        given()
                .spec(requestSpec)
                .when()
                .get("/tasks/" + taskId)
                .then()
                .statusCode(404);
    }
}
