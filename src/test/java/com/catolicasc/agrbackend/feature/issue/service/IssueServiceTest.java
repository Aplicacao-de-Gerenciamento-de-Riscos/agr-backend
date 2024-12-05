package com.catolicasc.agrbackend.feature.issue.service;

import com.catolicasc.agrbackend.clients.jira.dto.JiraIssueResponseDTO;
import com.catolicasc.agrbackend.clients.jira.dto.JiraSprintResponseDTO;
import com.catolicasc.agrbackend.clients.jira.service.JiraAPI;
import com.catolicasc.agrbackend.feature.issue.domain.Issue;
import com.catolicasc.agrbackend.feature.issue.dto.IssueDTO;
import com.catolicasc.agrbackend.feature.issue.repository.IssueRepository;
import com.catolicasc.agrbackend.feature.sprint.domain.Sprint;
import com.catolicasc.agrbackend.feature.sprint.service.SprintService;
import com.catolicasc.agrbackend.feature.version.service.VersionService;
import com.catolicasc.agrbackend.feature.worklog.service.WorklogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class IssueServiceTest {

    @Mock
    private IssueRepository issueRepository;

    @Mock
    private JiraAPI jiraAPI;

    @Mock
    private SprintService sprintService;

    @Mock
    private WorklogService worklogService;

    @Mock
    private VersionService versionService;

    @Mock
    Issue existingIssue;

    @InjectMocks
    private IssueService issueService;

    private Sprint sprint;
    private JiraIssueResponseDTO jiraIssueResponseDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        sprint = new Sprint();
        sprint.setId(1L);

        // Mocking the Jira API response
        jiraIssueResponseDTO = new JiraIssueResponseDTO();
        JiraIssueResponseDTO.IssueResponse issueResponse = new JiraIssueResponseDTO.IssueResponse();
        issueResponse.setId("1");
        issueResponse.setKey("Issue 1");
        jiraIssueResponseDTO.setTotal(1);
        jiraIssueResponseDTO.setIssues(List.of(issueResponse));
    }

    @Test
    void testUpdateIssueIfChanged() {
        Issue existingIssue = new Issue();
        existingIssue.setKey("OLD_KEY");
        existingIssue.setStatus("OLD_STATUS");

        IssueDTO newIssueDTO = new IssueDTO();
        newIssueDTO.setKey("NEW_KEY");
        newIssueDTO.setStatus("NEW_STATUS");

        issueService.updateIssueIfChanged(existingIssue, newIssueDTO);

        assert Objects.equals(existingIssue.getKey(), newIssueDTO.getKey());
        assert Objects.equals(existingIssue.getStatus(), newIssueDTO.getStatus());
    }

    @Test
    void testGetIssueDTO() {
        ResponseEntity<JiraIssueResponseDTO> responseEntity = ResponseEntity.ok(jiraIssueResponseDTO);
        // Mock the Jira API to return a response with issues
        lenient().when(jiraAPI.listIssuesBySprint(any(), anyLong())).thenReturn(responseEntity);

        // Call the method to get IssueDTO
        List<IssueDTO> issueDTOS = issueService.getIssueDTO(jiraIssueResponseDTO, sprint);

        // Verify that the conversion of issues was successful
        assert issueDTOS.size() > 0;
    }
}