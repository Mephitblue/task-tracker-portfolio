package com.keithlamond.tasktracker.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserRequest {

    private String name;
    private String email;
}
