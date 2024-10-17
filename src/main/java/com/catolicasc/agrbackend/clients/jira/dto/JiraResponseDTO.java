package com.catolicasc.agrbackend.clients.jira.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class JiraResponseDTO {
    private String expand;
    private int startAt;
    private int maxResults;
    private int total;
}
