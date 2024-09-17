package com.catolicasc.agrbackend.feature.issue.service;

import com.catolicasc.agrbackend.clients.jira.dto.JiraIssueResponseDTO;
import com.catolicasc.agrbackend.clients.jira.service.JiraAPI;
import com.catolicasc.agrbackend.feature.issue.repository.IssueRepository;
import org.springframework.stereotype.Service;

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
}
