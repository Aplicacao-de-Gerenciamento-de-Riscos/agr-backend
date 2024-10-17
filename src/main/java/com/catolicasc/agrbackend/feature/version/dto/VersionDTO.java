package com.catolicasc.agrbackend.feature.version.dto;

import com.catolicasc.agrbackend.feature.project.dto.ProjectDTO;
import lombok.Data;

import java.time.LocalDate;

@Data
public class VersionDTO {
    private Long id;
    private String description;
    private String name;
    private Boolean archived;
    private Boolean released;
    private LocalDate startDate;
    private LocalDate releaseDate;
    private Boolean overdue;
    private String userStartDate;
    private String userReleaseDate;
    private ProjectDTO project;
}
