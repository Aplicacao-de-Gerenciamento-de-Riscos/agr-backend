package com.catolicasc.agrbackend.clients.jira.service;

import com.catolicasc.agrbackend.clients.jira.dto.JiraIssueResponseDTO;
import com.catolicasc.agrbackend.clients.jira.dto.JiraSprintResponseDTO;
import com.catolicasc.agrbackend.clients.jira.dto.JiraVersionResponseDTO;
import com.catolicasc.agrbackend.config.feignclient.FeignClientJiraConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "jira", url = "${JIRA_API_URL}", configuration = FeignClientJiraConfiguration.class)
public interface JiraAPI {
    @GetMapping(value = "/agile/1.0/sprint/{sprintId}/issue")
    ResponseEntity<JiraIssueResponseDTO> listIssuesBySprint(@PathVariable String sprintId, @RequestParam Long maxResults);

    @GetMapping(value = "/agile/1.0/board/{boardId}/sprint")
    ResponseEntity<JiraSprintResponseDTO> listSprintsByBoard(@PathVariable String boardId, @RequestParam Long startAt);

    @GetMapping(value = "/api/latest/project/{projectKey}/versions")
    ResponseEntity<List<JiraVersionResponseDTO>> listVersionsByProject(@PathVariable String projectKey);
}
