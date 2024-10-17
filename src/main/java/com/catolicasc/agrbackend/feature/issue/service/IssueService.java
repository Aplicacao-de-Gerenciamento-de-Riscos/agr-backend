package com.catolicasc.agrbackend.feature.issue.service;

import com.catolicasc.agrbackend.clients.jira.dto.JiraIssueResponseDTO;
import com.catolicasc.agrbackend.clients.jira.service.JiraAPI;
import com.catolicasc.agrbackend.feature.component.service.ComponentService;
import com.catolicasc.agrbackend.feature.epic.service.EpicService;
import com.catolicasc.agrbackend.feature.issue.domain.Issue;
import com.catolicasc.agrbackend.feature.issue.dto.IssueDTO;
import com.catolicasc.agrbackend.feature.issue.repository.IssueRepository;
import com.catolicasc.agrbackend.feature.sprint.domain.Sprint;
import com.catolicasc.agrbackend.feature.sprint.service.SprintService;
import com.catolicasc.agrbackend.feature.version.dto.VersionDTO;
import com.catolicasc.agrbackend.feature.version.service.VersionService;
import com.catolicasc.agrbackend.feature.versionissue.domain.VersionIssue;
import com.catolicasc.agrbackend.feature.versionissue.service.VersionIssueService;
import com.catolicasc.agrbackend.feature.worklog.service.WorklogService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static java.util.Objects.nonNull;

@Service
public class IssueService {

    private final IssueRepository issueRepository;
    private final JiraAPI jiraAPI;
    private final ComponentService componentService;
    private final SprintService sprintService;
    private final EpicService epicService;
    private final WorklogService worklogService;
    private final VersionService versionService;
    private final VersionIssueService versionIssueService;

    public IssueService(
            IssueRepository issueRepository,
            JiraAPI jiraAPI,
            ComponentService componentService,
            SprintService sprintService, EpicService epicService, WorklogService worklogService, VersionService versionService, VersionIssueService versionIssueService) {
        this.issueRepository = issueRepository;
        this.jiraAPI = jiraAPI;
        this.componentService = componentService;
        this.sprintService = sprintService;
        this.epicService = epicService;
        this.worklogService = worklogService;
        this.versionService = versionService;
        this.versionIssueService = versionIssueService;
    }

    public JiraIssueResponseDTO listIssuesBySprint(String sprintId) {
        return jiraAPI.listIssuesBySprint(sprintId, 1000L).getBody();
    }

    public void syncIssuesBySprints() {
        List<Sprint> sprints = sprintService.findAll();
        sprints.forEach(sprint -> {
            JiraIssueResponseDTO jiraIssueResponseDTO = listIssuesBySprint(sprint.getId().toString());
            List<IssueDTO> issueDTOS = getIssueDTO(jiraIssueResponseDTO, sprint);
            List<Issue> issues = new ArrayList<>();
            Set<Long> processedIds = new HashSet<>();
            issueDTOS.forEach(issueDTO -> {
                Issue issue = toDomain(issueDTO, processedIds);
                if (nonNull(issue)) {
                    issues.add(issue);
                }
            });
            issues.forEach(issue -> {
                if (nonNull(issue.getVersionIssues()) && !issue.getVersionIssues().isEmpty()) {
                    List<VersionIssue> versionIssues = issue.getVersionIssues();
                    issue.setVersionIssues(null);
                    issueRepository.save(issue);
                    versionIssueService.saveAll(versionIssues);
                } else {
                    issueRepository.save(issue);
                }
            });
        });
    }

    public List<IssueDTO> getIssueDTO(JiraIssueResponseDTO jiraIssueResponseDTO, Sprint sprint) {
        List<IssueDTO> issueDTOS = new ArrayList<>();

        jiraIssueResponseDTO.getIssues().forEach(jiraIssueResponseDTO1 -> {
            IssueDTO issueDTO = new IssueDTO();
            issueDTO.setId(Long.parseLong(jiraIssueResponseDTO1.getId()));
            issueDTO.setKey(jiraIssueResponseDTO1.getKey());
            issueDTO.setStatus(jiraIssueResponseDTO1.getFields().getStatus().getName());
            issueDTO.setAssignee(nonNull(jiraIssueResponseDTO1.getFields().getAssignee()) && nonNull(jiraIssueResponseDTO1.getFields().getAssignee().getEmailAddress()) ? jiraIssueResponseDTO1.getFields().getAssignee().getEmailAddress() : "UNASSIGNED");
            issueDTO.setPriority(jiraIssueResponseDTO1.getFields().getPriority().getName());
            issueDTO.setIssueType(jiraIssueResponseDTO1.getFields().getIssuetype().getName());
            issueDTO.setSummary(jiraIssueResponseDTO1.getFields().getSummary());
            issueDTO.setTimespent(jiraIssueResponseDTO1.getFields().getTimespent());
            issueDTO.setTimeEstimate(jiraIssueResponseDTO1.getFields().getTimeestimate());
            issueDTO.setTimeOriginalEstimate(jiraIssueResponseDTO1.getFields().getTimeoriginalestimate());
            issueDTO.setWorkRatio(jiraIssueResponseDTO1.getFields().getWorkratio());
            issueDTO.setWorklog(worklogService.toDTO(nonNull(jiraIssueResponseDTO1.getFields().getWorklog()) ? jiraIssueResponseDTO1.getFields().getWorklog() : new JiraIssueResponseDTO.Worklog()));
            issueDTO.setComponents(jiraIssueResponseDTO1.getFields().getComponents().stream().map(componentService::toDto).toList());
            issueDTO.setSprint(sprintService.toDto(sprint));
            issueDTO.setEpic(nonNull(jiraIssueResponseDTO1.getFields().getEpic()) ? epicService.toDto(jiraIssueResponseDTO1.getFields().getEpic()) : null);
            issueDTO.setResolutionDate(nonNull(jiraIssueResponseDTO1.getFields().getResolutiondate()) ? LocalDateTime.parse(jiraIssueResponseDTO1.getFields().getResolutiondate(), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")) : null);
            issueDTO.setUpdated(nonNull(jiraIssueResponseDTO1.getFields().getUpdated()) ? LocalDateTime.parse(jiraIssueResponseDTO1.getFields().getUpdated(), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")) : null);
            issueDTO.setCreated(nonNull(jiraIssueResponseDTO1.getFields().getCreated()) ? LocalDateTime.parse(jiraIssueResponseDTO1.getFields().getCreated(), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")) : null);
            issueDTO.setFlagged(jiraIssueResponseDTO1.getFields().isFlagged());

            if (nonNull(jiraIssueResponseDTO1.getFields().getParent())) {
                Issue parent = findIssueById(Long.parseLong(jiraIssueResponseDTO1.getFields().getParent().getId()));
                if (nonNull(parent)) {
                    issueDTO.setParent(toDto(parent));
                } else {
                    issueDTO.setParent(toDto(issueRepository.save(getIssueByParent(jiraIssueResponseDTO1.getFields().getParent()))));
                }
            }

            if (nonNull(jiraIssueResponseDTO1.getFields().getFixVersions())) {
                List<VersionDTO> versionDTOS = new ArrayList<>();
                jiraIssueResponseDTO1.getFields().getFixVersions().forEach(version -> {
                    versionDTOS.add(versionService.toDTO(versionService.findById(Long.parseLong(version.getId()))));
                });
                issueDTO.setVersion(versionDTOS);
            }

            issueDTOS.add(issueDTO);
        });

        return issueDTOS;
    }

    public Issue findIssueById(Long id) {
        return issueRepository.findById(id).orElse(null);
    }

    public Issue getIssueByParent(JiraIssueResponseDTO.Parent parent) {
        return Issue.builder()
                .id(Long.parseLong(parent.getId()))
                .key(parent.getKey())
                .issueType(parent.getFields().getIssuetype().getName())
                .priority(parent.getFields().getPriority().getName())
                .summary(parent.getFields().getSummary())
                .status(parent.getFields().getStatus().getName()).build();
    }

    public IssueDTO toDto(Issue issue) {
        IssueDTO issueDTO = new IssueDTO();
        BeanUtils.copyProperties(issue, issueDTO);
        return issueDTO;
    }

    public Issue toDomain(IssueDTO issueDTO, Set<Long> processedIds) {
        if (processedIds.contains(issueDTO.getId())) {
            return null;
        }

        processedIds.add(issueDTO.getId());
        Issue issue = new Issue();
        BeanUtils.copyProperties(issueDTO, issue);
        issue.setComponents(nonNull(issueDTO.getComponents()) ? issueDTO.getComponents().stream().map(componentService::toDomain).toList() : null);
        issue.setSprint(nonNull(issueDTO.getSprint()) ? sprintService.toDomain(issueDTO.getSprint()) : null);
        issue.setEpic(nonNull(issueDTO.getEpic()) ? epicService.toDomain(issueDTO.getEpic()) : null);
        issue.setWorklog(nonNull(issueDTO.getWorklog()) ? worklogService.toDomain(issueDTO.getWorklog()) : null);

        if (nonNull(issueDTO.getVersion()) && !issueDTO.getVersion().isEmpty()) {
            List<VersionIssue> versionIssues = new ArrayList<>();
            issueDTO.getVersion().forEach(versionDTO -> {
                VersionIssue versionIssue = VersionIssue.builder().issue(issue).version(versionService.findById(versionDTO.getId())).build();
                versionIssues.add(versionIssue);
            });
            issue.setVersionIssues(versionIssues);
        }

        if (issueDTO.getParent() != null) {
            issue.setParent(toDomain(issueDTO.getParent(), processedIds));
        }

        return issue;
    }
}
