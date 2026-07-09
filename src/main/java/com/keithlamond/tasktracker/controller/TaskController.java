package com.keithlamond.tasktracker.controller;

import com.keithlamond.tasktracker.dto.TaskRequest;
import com.keithlamond.tasktracker.dto.TaskResponse;
import com.keithlamond.tasktracker.dto.TaskStatusRequest;
import com.keithlamond.tasktracker.dto.TaskUpdateRequest;
import com.keithlamond.tasktracker.entity.Priority;
import com.keithlamond.tasktracker.entity.Status;
import com.keithlamond.tasktracker.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@RequestBody TaskRequest request) {
        TaskResponse response = taskService.createTask(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTaskById(@PathVariable Long id) {
        TaskResponse response = taskService.getTaskById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<TaskResponse>> getTasks(
            @RequestParam(required = false) Status status,
            @RequestParam(required = false) Priority priority,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String order) {
        List<TaskResponse> response = taskService.getTasks(status, priority, userId, sortBy, order);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable Long id,
            @RequestBody TaskUpdateRequest request) {
        TaskResponse response = taskService.updateTask(id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TaskResponse> transitionStatus(
            @PathVariable Long id,
            @RequestBody TaskStatusRequest request) {
        TaskResponse response = taskService.transitionStatus(id, request.getStatus());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}

