package com.catolicasc.agrbackend.feature.issue.service;

import com.catolicasc.agrbackend.clients.jira.dto.JiraIssueResponseDTO;
import com.catolicasc.agrbackend.clients.jira.service.JiraAPI;
import com.catolicasc.agrbackend.feature.issue.domain.Issue;
import com.catolicasc.agrbackend.feature.issue.dto.IssueDTO;
import com.catolicasc.agrbackend.feature.issue.repository.IssueRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.nonNull;

@Service
public class IssueService {

    private final IssueRepository issueRepository;
    private final JiraAPI jiraAPI;

    public IssueService(
            IssueRepository issueRepository,
            JiraAPI jiraAPI
    ) {
        this.issueRepository = issueRepository;
        this.jiraAPI = jiraAPI;
    }

    public JiraIssueResponseDTO listIssuesBySprint(String sprintId) {
        return jiraAPI.listIssuesBySprint(sprintId).getBody();
    }

    public void syncIssuesBySprint(String sprintId) {
        JiraIssueResponseDTO jiraIssueResponseDTO = listIssuesBySprint(sprintId);


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
            //TODO: 
//            issueDTO.setWorkLog(jiraIssueResponseDTO1.getFields().getWorklog().getTotal());
            //TODO: set componentDTO, sprintDTO and epicDTO
            issueDTO.setParent(nonNull(jiraIssueResponseDTO1.getFields().getParent().getId()) ? );
        });

        return null;
    }

    public Issue findIssueById(Long id) {
        return issueRepository.findById(id).orElse(null);
    }
}
