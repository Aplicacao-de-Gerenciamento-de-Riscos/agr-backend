package com.catolicasc.agrbackend.feature.issue.dto;

import com.catolicasc.agrbackend.feature.component.dto.ComponentDTO;
import com.catolicasc.agrbackend.feature.epic.dto.EpicDTO;
import com.catolicasc.agrbackend.feature.issue.domain.Issue;
import com.catolicasc.agrbackend.feature.sprint.dto.SprintDTO;
import com.catolicasc.agrbackend.feature.worklog.dto.WorklogDTO;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class IssueDTO {
    private Long id;
    private String key;
    private Long timeOriginalEstimate;
    private Long timeEstimate;
    private Long workRatio;
    private WorklogDTO workLog;
    private String status;
    private Long timespent;
    private LocalDateTime resolutionDate;
    private LocalDateTime updated;
    private LocalDateTime created;
    private Boolean flagged;
    private String assignee;
    private String priority;
    private String issueType;
    private String summary;
    private List<ComponentDTO> components;
    private EpicDTO epic;
    private SprintDTO sprint;
    private IssueDTO parent;
    private WorklogDTO worklog;
}
