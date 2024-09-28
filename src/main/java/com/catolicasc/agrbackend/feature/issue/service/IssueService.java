package com.catolicasc.agrbackend.feature.issue.service;

import com.catolicasc.agrbackend.clients.jira.dto.JiraIssueResponseDTO;
import com.catolicasc.agrbackend.clients.jira.service.JiraAPI;
import com.catolicasc.agrbackend.feature.component.service.ComponentService;
import com.catolicasc.agrbackend.feature.epic.service.EpicService;
import com.catolicasc.agrbackend.feature.issue.domain.Issue;
import com.catolicasc.agrbackend.feature.issue.dto.IssueDTO;
import com.catolicasc.agrbackend.feature.issue.repository.IssueRepository;
import com.catolicasc.agrbackend.feature.sprint.service.SprintService;
import com.catolicasc.agrbackend.feature.worklog.service.WorklogService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Objects.nonNull;

@Service
public class IssueService {

    private final IssueRepository issueRepository;
    private final JiraAPI jiraAPI;
    private final ComponentService componentService;
    private final SprintService sprintService;
    private final EpicService epicService;
    private final WorklogService worklogService;

    public IssueService(
            IssueRepository issueRepository,
            JiraAPI jiraAPI,
            ComponentService componentService,
            SprintService sprintService, EpicService epicService, WorklogService worklogService) {
        this.issueRepository = issueRepository;
        this.jiraAPI = jiraAPI;
        this.componentService = componentService;
        this.sprintService = sprintService;
        this.epicService = epicService;
        this.worklogService = worklogService;
    }

    public JiraIssueResponseDTO listIssuesBySprint(String sprintId) {
        return jiraAPI.listIssuesBySprint(sprintId).getBody();
    }

    public void syncIssuesBySprint(String sprintId) {
        JiraIssueResponseDTO jiraIssueResponseDTO = listIssuesBySprint(sprintId);
        List<IssueDTO> issueDTOS = getIssueDTO(jiraIssueResponseDTO);
        Set<Long> processedIds = new HashSet<>();
        issueRepository.saveAll(issueDTOS.stream().map(issueDTO -> toDomain(issueDTO, processedIds)).toList());
    }

    public List<IssueDTO> getIssueDTO(JiraIssueResponseDTO jiraIssueResponseDTO) {
        List<IssueDTO> issueDTOS = new ArrayList<>();

        jiraIssueResponseDTO.getIssues().forEach(jiraIssueResponseDTO1 -> {
            IssueDTO issueDTO = new IssueDTO();
            issueDTO.setId(Long.parseLong(jiraIssueResponseDTO1.getId()));
            issueDTO.setKey(jiraIssueResponseDTO1.getKey());
            issueDTO.setStatus(jiraIssueResponseDTO1.getFields().getStatus().getName());
            issueDTO.setAssignee(jiraIssueResponseDTO1.getFields().getAssignee().getEmailAddress());
            issueDTO.setPriority(jiraIssueResponseDTO1.getFields().getPriority().getName());
            issueDTO.setIssueType(jiraIssueResponseDTO1.getFields().getIssuetype().getName());
            issueDTO.setSummary(jiraIssueResponseDTO1.getFields().getSummary());
            issueDTO.setTimespent(jiraIssueResponseDTO1.getFields().getTimespent());
            issueDTO.setTimeEstimate(jiraIssueResponseDTO1.getFields().getTimeestimate());
            issueDTO.setTimeOriginalEstimate(jiraIssueResponseDTO1.getFields().getTimeoriginalestimate());
            issueDTO.setWorkRatio(jiraIssueResponseDTO1.getFields().getWorkratio());
            issueDTO.setWorklog(worklogService.toDTO(nonNull(jiraIssueResponseDTO1.getFields().getWorklog()) ? jiraIssueResponseDTO1.getFields().getWorklog() : new JiraIssueResponseDTO.Worklog()));
            issueDTO.setComponents(jiraIssueResponseDTO1.getFields().getComponents().stream().map(componentService::toDto).toList());
            issueDTO.setSprint(nonNull(jiraIssueResponseDTO1.getFields().getSprint()) ? sprintService.toDto(jiraIssueResponseDTO1.getFields().getSprint()) : null);
            issueDTO.setEpic(nonNull(jiraIssueResponseDTO1.getFields().getEpic()) ? epicService.toDto(jiraIssueResponseDTO1.getFields().getEpic()) : null);

            if (nonNull(jiraIssueResponseDTO1.getFields().getParent())) {
                Issue parent = findIssueById(Long.parseLong(jiraIssueResponseDTO1.getFields().getParent().getId()));
                if (nonNull(parent)) {
                    issueDTO.setParent(toDto(parent));
                } else {
                    issueDTO.setParent(toDto(issueRepository.save(getIssueByParent(jiraIssueResponseDTO1.getFields().getParent()))));
                }
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
        issue.setEpic(nonNull(issue.getEpic()) ? epicService.toDomain(issueDTO.getEpic()) : null);
        issue.setWorklog(nonNull(issueDTO.getWorklog()) ? worklogService.toDomain(issueDTO.getWorklog()) : null);

        if (issueDTO.getParent() != null) {
            issue.setParent(toDomain(issueDTO.getParent(), processedIds));
        }

        return issue;
    }
}
