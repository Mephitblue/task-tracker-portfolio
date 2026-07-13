
package com.keithlamond.tasktracker.tests;

import com.keithlamond.tasktracker.base.BaseTest;
import com.keithlamond.tasktracker.model.TaskRequest;
import com.keithlamond.tasktracker.model.UserRequest;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class TaskStatusTests extends BaseTest {

    private Long userId;

    @BeforeClass
    public void createTestUser() {
        UserRequest userRequest = new UserRequest("Task Status User", "taskstatus@example.com");

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

    private Long createTask() {
        TaskRequest request = new TaskRequest("Status Test Task", null, null, userId);

        Response response = given()
                .spec(requestSpec)
                .body(request)
                .when()
                .post("/tasks")
                .then()
                .statusCode(201)
                .extract().response();

        return response.jsonPath().getLong("id");
    }

    private void transitionStatus(Long taskId, String status, int expectedStatusCode) {
        String body = "{\"status\": \"" + status + "\"}";

        given()
                .spec(requestSpec)
                .body(body)
                .when()
                .patch("/tasks/" + taskId + "/status")
                .then()
                .statusCode(expectedStatusCode);
    }

    // ─── Valid Transitions ────────────────────────────────────────────────────

    @Test
    public void transitionStatus_OpenToInProgress_Returns200() {
        Long taskId = createTask();

        given()
                .spec(requestSpec)
                .body("{\"status\": \"IN_PROGRESS\"}")
                .when()
                .patch("/tasks/" + taskId + "/status")
                .then()
                .statusCode(200)
                .body("status", equalTo("IN_PROGRESS"));
    }

    @Test
    public void transitionStatus_InProgressToDone_Returns200() {
        Long taskId = createTask();
        transitionStatus(taskId, "IN_PROGRESS", 200);

        given()
                .spec(requestSpec)
                .body("{\"status\": \"DONE\"}")
                .when()
                .patch("/tasks/" + taskId + "/status")
                .then()
                .statusCode(200)
                .body("status", equalTo("DONE"));
    }

    @Test
    public void transitionStatus_InProgressToOpen_Returns200() {
        Long taskId = createTask();
        transitionStatus(taskId, "IN_PROGRESS", 200);

        given()
                .spec(requestSpec)
                .body("{\"status\": \"OPEN\"}")
                .when()
                .patch("/tasks/" + taskId + "/status")
                .then()
                .statusCode(200)
                .body("status", equalTo("OPEN"));
    }

    // ─── Invalid Transitions ──────────────────────────────────────────────────

    @Test
    public void transitionStatus_OpenToDone_Returns409() {
        Long taskId = createTask();

        given()
                .spec(requestSpec)
                .body("{\"status\": \"DONE\"}")
                .when()
                .patch("/tasks/" + taskId + "/status")
                .then()
                .statusCode(409);
    }

    @Test
    public void transitionStatus_DoneToOpen_Returns409() {
        Long taskId = createTask();
        transitionStatus(taskId, "IN_PROGRESS", 200);
        transitionStatus(taskId, "DONE", 200);

        given()
                .spec(requestSpec)
                .body("{\"status\": \"OPEN\"}")
                .when()
                .patch("/tasks/" + taskId + "/status")
                .then()
                .statusCode(409);
    }

    @Test
    public void transitionStatus_DoneToInProgress_Returns409() {
        Long taskId = createTask();
        transitionStatus(taskId, "IN_PROGRESS", 200);
        transitionStatus(taskId, "DONE", 200);

        given()
                .spec(requestSpec)
                .body("{\"status\": \"IN_PROGRESS\"}")
                .when()
                .patch("/tasks/" + taskId + "/status")
                .then()
                .statusCode(409);
    }

    // ─── Edge Cases ───────────────────────────────────────────────────────────

    @Test
    public void transitionStatus_InvalidTaskId_Returns404() {
        given()
                .spec(requestSpec)
                .body("{\"status\": \"IN_PROGRESS\"}")
                .when()
                .patch("/tasks/999999/status")
                .then()
                .statusCode(404);
    }

    @Test
    public void transitionStatus_InvalidStatusValue_Returns400() {
        Long taskId = createTask();

        given()
                .spec(requestSpec)
                .body("{\"status\": \"INVALID\"}")
                .when()
                .patch("/tasks/" + taskId + "/status")
                .then()
                .statusCode(400);
    }
}