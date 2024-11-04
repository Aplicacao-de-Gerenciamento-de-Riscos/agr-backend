package com.catolicasc.agrbackend.feature.jobs.service;

import com.catolicasc.agrbackend.feature.issue.service.IssueService;
import com.catolicasc.agrbackend.feature.project.domain.Project;
import com.catolicasc.agrbackend.feature.project.service.ProjectService;
import com.catolicasc.agrbackend.feature.sprint.service.SprintService;
import com.catolicasc.agrbackend.feature.version.service.VersionService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobsService {
    private final SprintService sprintService;
    private final VersionService versionService;
    private final IssueService issueService;
    private final ProjectService projectService;

    public JobsService(SprintService sprintService, VersionService versionService, IssueService issueService, ProjectService projectService) {
        this.sprintService = sprintService;
        this.versionService = versionService;
        this.issueService = issueService;
        this.projectService = projectService;
    }

    @Scheduled(cron = "0 0 0 * * *", zone = "America/Sao_Paulo")
    public void syncIssues() {
        List<Project> projects = projectService.findAll();
        versionService.syncVersions(projects);
        projects.forEach(project -> sprintService.syncSprintsByBoard(project.getBoardId().toString()));
        issueService.syncIssuesBySprints();
    }
}
