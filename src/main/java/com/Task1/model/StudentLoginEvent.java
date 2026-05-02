package com.Task1.model;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentLoginEvent {

    private String email;
    private String role;
    private String message;
    private LocalDateTime loginTime;

}
