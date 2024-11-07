package com.catolicasc.agrbackend.feature.issue.dto;

import lombok.Data;

@Data
public class IssuePlanningDTO {
    private Long totalDoneIssues;
    private Long totalOtherStatusIssues;
    private Long totalIssues;

    public IssuePlanningDTO(Long totalDoneIssues, Long totalOtherStatusIssues, Long totalIssues) {
        this.totalDoneIssues = totalDoneIssues;
        this.totalOtherStatusIssues = totalOtherStatusIssues;
        this.totalIssues = totalIssues;
    }
}
