package com.catolicasc.agrbackend.clients.jira.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class JiraSprintResponseDTO extends JiraResponseDTO {
    @JsonProperty("isLast")
    private boolean isLast;
    private List<SprintResponse> values;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SprintResponse {
        private String id;
        private String self;
        private String state;
        private String name;
        private String startDate;
        private String endDate;
        private String completeDate;
        private String goal;
    }
}
