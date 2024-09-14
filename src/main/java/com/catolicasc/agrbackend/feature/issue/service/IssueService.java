package com.catolicasc.agrbackend.feature.issue.service;

import com.catolicasc.agrbackend.clients.jira.dto.JiraIssueResponseDTO;
import com.catolicasc.agrbackend.clients.jira.service.JiraAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("v1/issue")
public class IssueService {

    @Autowired
    private JiraAPI jiraAPI;

    @GetMapping("/sprint/{sprintId}")
    public ResponseEntity<JiraIssueResponseDTO> testListIssuesBySprint(@PathVariable String sprintId) {
        // Chama o Feign Client para o Jira
        ResponseEntity<JiraIssueResponseDTO> response = jiraAPI.listIssuesBySprint(sprintId);

        // Retorna a resposta
        return response;
    }
}
