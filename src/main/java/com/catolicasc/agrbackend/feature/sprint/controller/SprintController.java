package com.catolicasc.agrbackend.feature.sprint.controller;

import com.catolicasc.agrbackend.feature.project.domain.Project;
import com.catolicasc.agrbackend.feature.project.service.ProjectService;
import com.catolicasc.agrbackend.feature.sprint.domain.Sprint;
import com.catolicasc.agrbackend.feature.sprint.dto.SprintDTO;
import com.catolicasc.agrbackend.feature.sprint.service.SprintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/v1/sprint")
public class SprintController {

    @Autowired
    private SprintService sprintService;

    @Autowired
    private ProjectService projectService;

    @GetMapping("/all")
    public ResponseEntity<List<SprintDTO>> getAllSprints(@RequestParam(name = "projectId") Long projectId) {
        Project project = projectService.findById(projectId);
        List<SprintDTO> sprints = new ArrayList<>();
        List<Sprint> sprintsDomain = sprintService.findAllByProjectKey(project.getKey());
        sprintsDomain.forEach(sprint -> sprints.add(sprintService.getSprintDTO(sprint.getId())));
        return ResponseEntity.ok(sprints);
    }
}
