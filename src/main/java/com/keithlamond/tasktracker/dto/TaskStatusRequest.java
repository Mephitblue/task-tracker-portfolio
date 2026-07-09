package com.keithlamond.tasktracker.dto;

import com.keithlamond.tasktracker.entity.Status;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TaskStatusRequest {

    private Status status;
}