package com.catolicasc.agrbackend.clients.jira.service;

import com.catolicasc.agrbackend.clients.jira.dto.JiraIssueResponseDTO;
import com.catolicasc.agrbackend.config.feignclient.FeignClientJiraConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "jira", url = "${JIRA_API_URL}", configuration = FeignClientJiraConfiguration.class)
public interface JiraAPI {
    @GetMapping(value = "/sprint/{sprintId}/issue")
    ResponseEntity<JiraIssueResponseDTO> listIssuesBySprint(@PathVariable String sprintId);
}
