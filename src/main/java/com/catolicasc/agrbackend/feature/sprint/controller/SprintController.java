package com.catolicasc.agrbackend.feature.sprint.controller;

import com.catolicasc.agrbackend.feature.issue.service.IssueService;
import com.catolicasc.agrbackend.feature.project.domain.Project;
import com.catolicasc.agrbackend.feature.project.service.ProjectService;
import com.catolicasc.agrbackend.feature.sprint.service.SprintService;
import com.catolicasc.agrbackend.feature.version.service.VersionService;
import feign.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("v1/sprint")
public class SprintController {

    @Autowired
    private SprintService sprintService;
    @Autowired
    private VersionService versionService;
    @Autowired
    private ProjectService projectService;

    @GetMapping("/sync")
    public ResponseEntity<Response> syncIssuesBySprint() {
        List<Project> projects = projectService.findAll();
        versionService.syncVersions(projects);
        projects.forEach(project -> sprintService.syncSprintsByBoard(project.getBoardId().toString()));
        return ResponseEntity.ok().build();
    }

}
