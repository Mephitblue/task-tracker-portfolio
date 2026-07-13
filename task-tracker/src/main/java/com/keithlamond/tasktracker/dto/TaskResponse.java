package com.keithlamond.tasktracker.dto;

import com.keithlamond.tasktracker.entity.Priority;
import com.keithlamond.tasktracker.entity.Status;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {

    private Long id;
    private String title;
    private String description;
    private Status status;
    private Priority priority;
    private Long userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
