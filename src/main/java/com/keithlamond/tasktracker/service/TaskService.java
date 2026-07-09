package com.keithlamond.tasktracker.service;

import com.keithlamond.tasktracker.dto.TaskRequest;
import com.keithlamond.tasktracker.dto.TaskResponse;
import com.keithlamond.tasktracker.dto.TaskUpdateRequest;
import com.keithlamond.tasktracker.entity.Priority;
import com.keithlamond.tasktracker.entity.Status;
import com.keithlamond.tasktracker.entity.Task;
import com.keithlamond.tasktracker.entity.User;
import com.keithlamond.tasktracker.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserService userService;

    public TaskResponse createTask(TaskRequest request) {
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Title is required");
        }
        if (request.getUserId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userId is required");
        }

        User user = userService.getUserEntityById(request.getUserId());

        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setUser(user);

        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }

        Task saved = taskRepository.save(task);
        return toResponse(saved);
    }

    public TaskResponse getTaskById(Long id) {
        Task task = findTaskById(id);
        return toResponse(task);
    }

    public List<TaskResponse> getTasks(Status status, Priority priority, Long userId, String sortBy, String order) {
        List<Task> tasks;

        if (sortBy != null && sortBy.equalsIgnoreCase("priority")) {
            tasks = "desc".equalsIgnoreCase(order)
                    ? taskRepository.findAllByOrderByPriorityDesc()
                    : taskRepository.findAllByOrderByPriorityAsc();
        } else if (userId != null && status != null && priority != null) {
            tasks = taskRepository.findByUserIdAndStatusAndPriority(userId, status, priority);
        } else if (userId != null && status != null) {
            tasks = taskRepository.findByUserIdAndStatus(userId, status);
        } else if (userId != null && priority != null) {
            tasks = taskRepository.findByUserIdAndPriority(userId, priority);
        } else if (status != null && priority != null) {
            tasks = taskRepository.findByStatusAndPriority(status, priority);
        } else if (userId != null) {
            tasks = taskRepository.findByUserId(userId);
        } else if (status != null) {
            tasks = taskRepository.findByStatus(status);
        } else if (priority != null) {
            tasks = taskRepository.findByPriority(priority);
        } else {
            tasks = taskRepository.findAll();
        }

        return tasks.stream().map(this::toResponse).toList();
    }

    public TaskResponse updateTask(Long id, TaskUpdateRequest request) {
        Task task = findTaskById(id);

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            task.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }
        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }

        Task saved = taskRepository.save(task);
        return toResponse(saved);
    }

    public TaskResponse transitionStatus(Long id, Status requestedStatus) {
        Task task = findTaskById(id);
        Status currentStatus = task.getStatus();

        validateTransition(currentStatus, requestedStatus);

        task.setStatus(requestedStatus);
        Task saved = taskRepository.save(task);
        return toResponse(saved);
    }

    public void deleteTask(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Task not found with id: " + id);
        }
        taskRepository.deleteById(id);
    }

    private void validateTransition(Status current, Status requested) {
        boolean valid = switch (current) {
            case OPEN        -> requested == Status.IN_PROGRESS;
            case IN_PROGRESS -> requested == Status.OPEN || requested == Status.DONE;
            case DONE        -> false;
        };

        if (!valid) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Invalid status transition: " + current + " -> " + requested);
        }
    }

    private Task findTaskById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Task not found with id: " + id));
    }

    private TaskResponse toResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getUser().getId(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}
