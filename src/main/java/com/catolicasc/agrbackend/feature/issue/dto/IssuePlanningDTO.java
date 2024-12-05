package com.catolicasc.agrbackend.feature.issue.dto;

import lombok.Data;

@Data
public class IssuePlanningDTO {
    private Long totalDoneIssues;
    private Long totalOtherStatusIssues;
    private Long totalIssues;
    private Long totalTimeEstimateInDone;
    private Long totalTimeSpentInDone;
    private Long totalTimeEstimateOtherStatus;
    private Long totalTimeSpentOtherStatus;

    public IssuePlanningDTO(Long totalDoneIssues, Long totalOtherStatusIssues, Long totalIssues, Long totalTimeEstimateInDone, Long totalTimeSpentInDone, Long totalTimeEstimateOtherStatus, Long totalTimeSpentOtherStatus) {
        this.totalDoneIssues = totalDoneIssues;
        this.totalOtherStatusIssues = totalOtherStatusIssues;
        this.totalIssues = totalIssues;
        this.totalTimeEstimateInDone = totalTimeEstimateInDone;
        this.totalTimeSpentInDone = totalTimeSpentInDone;
        this.totalTimeEstimateOtherStatus = totalTimeEstimateOtherStatus;
        this.totalTimeSpentOtherStatus = totalTimeSpentOtherStatus;
    }
}
