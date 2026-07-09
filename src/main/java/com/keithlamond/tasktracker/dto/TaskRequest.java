package com.keithlamond.tasktracker.dto;

import com.keithlamond.tasktracker.entity.Priority;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TaskRequest {

    private String title;
    private String description;
    private Priority priority;
    private Long userId;
}