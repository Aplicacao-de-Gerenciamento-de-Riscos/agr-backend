package com.catolicasc.agrbackend.feature.sprint.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class SprintDTO {
    private Long id;
    private String name;
    private String state;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime completeDate;
    private String goal;
}
