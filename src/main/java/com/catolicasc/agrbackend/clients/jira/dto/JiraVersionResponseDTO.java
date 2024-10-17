package com.catolicasc.agrbackend.clients.jira.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class JiraVersionResponseDTO {
    private String self;
    private String id;
    private String name;
    private Boolean archived;
    private Boolean released;
    private String startDate;
    private String releaseDate;
    private Boolean overdue;
    private String userStartDate;
    private String userReleaseDate;
    private Long projectId;
    private String description;
}
