package com.catolicasc.agrbackend.feature.issue.controller;

import com.catolicasc.agrbackend.clients.jira.dto.JiraIssueResponseDTO;
import com.catolicasc.agrbackend.clients.jira.service.JiraAPI;
import com.catolicasc.agrbackend.feature.issue.service.IssueService;
import feign.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("v1/issue")
public class IssueController {

    @Autowired
    private IssueService issueService;

    @GetMapping("/sprint/{sprintId}")
    public ResponseEntity<JiraIssueResponseDTO> testListIssuesBySprint(@PathVariable String sprintId) {
        // Chama o Feign Client para o Jira
        JiraIssueResponseDTO response = issueService.listIssuesBySprint(sprintId);

        // Retorna a resposta
        return ResponseEntity.ok(response);
    }

    @GetMapping("/sprint/{sprintId}/sync")
    public ResponseEntity<Response> syncIssuesBySprint(@PathVariable String sprintId) {
        // Chama o Feign Client para o Jira
        issueService.syncIssuesBySprint(sprintId);

        // Retorna a resposta
        return ResponseEntity.ok().build();
    }

}
