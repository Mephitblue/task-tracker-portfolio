package com.keithlamond.tasktracker.tests;

import com.keithlamond.tasktracker.base.BaseTest;
import com.keithlamond.tasktracker.model.TaskRequest;
import com.keithlamond.tasktracker.model.UserRequest;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class TaskCrudTests extends BaseTest {

    private Long userId;

    @BeforeClass
    public void createTestUser() {
        UserRequest userRequest = new UserRequest("Task Crud User", "taskcrud@example.com");

        Response response = given()
                .spec(requestSpec)
                .body(userRequest)
                .when()
                .post("/users")
                .then()
                .statusCode(201)
                .extract().response();

        userId = response.jsonPath().getLong("id");
    }

    // ─── Create Task ──────────────────────────────────────────────────────────

    @Test
    public void createTask_ValidPayload_Returns201WithDefaults() {
        TaskRequest request = new TaskRequest("Valid Task", "A valid task description", "HIGH", userId);

        given()
                .spec(requestSpec)
                .body(request)
                .when()
                .post("/tasks")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("title", equalTo("Valid Task"))
                .body("description", equalTo("A valid task description"))
                .body("priority", equalTo("HIGH"))
                .body("userId", equalTo(userId.intValue()))
                .body("createdAt", notNullValue())
                .body("updatedAt", notNullValue());
    }

    @Test
    public void createTask_DefaultStatusIsOpen() {
        TaskRequest request = new TaskRequest("Status Default Task", null, null, userId);

        given()
                .spec(requestSpec)
                .body(request)
                .when()
                .post("/tasks")
                .then()
                .statusCode(201)
                .body("status", equalTo("OPEN"));
    }

    @Test
    public void createTask_DefaultPriorityIsMedium() {
        TaskRequest request = new TaskRequest("Priority Default Task", null, null, userId);

        given()
                .spec(requestSpec)
                .body(request)
                .when()
                .post("/tasks")
                .then()
                .statusCode(201)
                .body("priority", equalTo("MEDIUM"));
    }

    @Test
    public void createTask_InvalidUserId_Returns404() {
        TaskRequest request = new TaskRequest("Orphan Task", null, null, 999999L);

        given()
                .spec(requestSpec)
                .body(request)
                .when()
                .post("/tasks")
                .then()
                .statusCode(404);
    }

    @Test
    public void createTask_MissingTitle_Returns400() {
        TaskRequest request = new TaskRequest(null, "No title here", "LOW", userId);

        given()
                .spec(requestSpec)
                .body(request)
                .when()
                .post("/tasks")
                .then()
                .statusCode(400);
    }

    @Test
    public void createTask_MissingUserId_Returns400() {
        TaskRequest request = new TaskRequest("No User Task", null, null, null);

        given()
                .spec(requestSpec)
                .body(request)
                .when()
                .post("/tasks")
                .then()
                .statusCode(400);
    }

    // ─── Get Task ─────────────────────────────────────────────────────────────

    @Test
    public void getTaskById_ValidId_Returns200WithCorrectData() {
        TaskRequest request = new TaskRequest("Get Task Test", "Get task description", "LOW", userId);

        Response createResponse = given()
                .spec(requestSpec)
                .body(request)
                .when()
                .post("/tasks")
                .then()
                .statusCode(201)
                .extract().response();

        Long taskId = createResponse.jsonPath().getLong("id");

        given()
                .spec(requestSpec)
                .when()
                .get("/tasks/" + taskId)
                .then()
                .statusCode(200)
                .body("id", equalTo(taskId.intValue()))
                .body("title", equalTo("Get Task Test"))
                .body("description", equalTo("Get task description"))
                .body("priority", equalTo("LOW"))
                .body("status", equalTo("OPEN"));
    }

    @Test
    public void getTaskById_InvalidId_Returns404() {
        given()
                .spec(requestSpec)
                .when()
                .get("/tasks/999999")
                .then()
                .statusCode(404);
    }

    // ─── Update Task ──────────────────────────────────────────────────────────

    @Test
    public void updateTask_ValidPayload_Returns200() {
        TaskRequest request = new TaskRequest("Original Title", "Original description", "LOW", userId);

        Response createResponse = given()
                .spec(requestSpec)
                .body(request)
                .when()
                .post("/tasks")
                .then()
                .statusCode(201)
                .extract().response();

        Long taskId = createResponse.jsonPath().getLong("id");

        String updateBody = "{\"title\": \"Updated Title\", \"description\": \"Updated description\", \"priority\": \"HIGH\"}";

        given()
                .spec(requestSpec)
                .body(updateBody)
                .when()
                .put("/tasks/" + taskId)
                .then()
                .statusCode(200)
                .body("title", equalTo("Updated Title"))
                .body("description", equalTo("Updated description"))
                .body("priority", equalTo("HIGH"));
    }

    @Test
    public void updateTask_UpdatesUpdatedAtTimestamp() throws InterruptedException {
        TaskRequest request = new TaskRequest("Timestamp Task", null, null, userId);

        Response createResponse = given()
                .spec(requestSpec)
                .body(request)
                .when()
                .post("/tasks")
                .then()
                .statusCode(201)
                .extract().response();

        Long taskId = createResponse.jsonPath().getLong("id");
        String originalUpdatedAt = createResponse.jsonPath().getString("updatedAt");

        // Brief pause to ensure timestamp difference is detectable
        Thread.sleep(100);

        String updateBody = "{\"title\": \"Updated Timestamp Task\"}";

        given()
                .spec(requestSpec)
                .body(updateBody)
                .when()
                .put("/tasks/" + taskId)
                .then()
                .statusCode(200)
                .body("updatedAt", not(equalTo(originalUpdatedAt)));
    }

    @Test
    public void updateTask_InvalidId_Returns404() {
        String updateBody = "{\"title\": \"Ghost Task\"}";

        given()
                .spec(requestSpec)
                .body(updateBody)
                .when()
                .put("/tasks/999999")
                .then()
                .statusCode(404);
    }

    // ─── Delete Task ──────────────────────────────────────────────────────────

    @Test
    public void deleteTask_ValidId_Returns204() {
        TaskRequest request = new TaskRequest("Delete Task Test", null, null, userId);

        Response createResponse = given()
                .spec(requestSpec)
                .body(request)
                .when()
                .post("/tasks")
                .then()
                .statusCode(201)
                .extract().response();

        Long taskId = createResponse.jsonPath().getLong("id");

        given()
                .spec(requestSpec)
                .when()
                .delete("/tasks/" + taskId)
                .then()
                .statusCode(204);
    }

    @Test
    public void deleteTask_InvalidId_Returns404() {
        given()
                .spec(requestSpec)
                .when()
                .delete("/tasks/999999")
                .then()
                .statusCode(404);
    }
}
