package com.catolicasc.agrbackend.feature.issue.controller;

import com.catolicasc.agrbackend.clients.jira.dto.JiraIssueResponseDTO;
import com.catolicasc.agrbackend.clients.jira.service.JiraAPI;
import com.catolicasc.agrbackend.feature.issue.service.IssueService;
import com.catolicasc.agrbackend.feature.project.domain.Project;
import com.catolicasc.agrbackend.feature.project.service.ProjectService;
import com.catolicasc.agrbackend.feature.sprint.service.SprintService;
import com.catolicasc.agrbackend.feature.version.service.VersionService;
import feign.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("v1/issue")
public class IssueController {

    @Autowired
    private IssueService issueService;
    @Autowired
    private SprintService sprintService;
    @Autowired
    private VersionService versionService;
    @Autowired
    private ProjectService projectService;

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
//        List<Project> projects = projectService.findAll();
//        versionService.syncVersions(projects);
//        projects.forEach(project -> sprintService.syncSprintsByBoard(project.getBoardId().toString()));
        issueService.syncIssuesBySprints();

        // Retorna a resposta
        return ResponseEntity.ok().build();
    }

}
