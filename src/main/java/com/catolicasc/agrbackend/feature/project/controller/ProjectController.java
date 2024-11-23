package com.catolicasc.agrbackend.feature.project.controller;

import com.catolicasc.agrbackend.feature.project.domain.Project;
import com.catolicasc.agrbackend.feature.project.dto.ProjectDTO;
import com.catolicasc.agrbackend.feature.project.service.ProjectService;
import feign.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/project")
public class ProjectController {

    @Autowired
    ProjectService projectService;

    @GetMapping(value = "/all")
    public ResponseEntity<List<ProjectDTO>> getAllProjects() {
        List<Project> projects = projectService.findAll();
        List<ProjectDTO> projectDTOS = projects.stream().map(projectService::toDTO).collect(Collectors.toList());
        return ResponseEntity.ok(projectDTOS);
    }
}
