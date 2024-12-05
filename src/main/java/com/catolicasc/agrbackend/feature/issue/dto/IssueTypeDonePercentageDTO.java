package com.catolicasc.agrbackend.feature.issue.dto;

import lombok.Data;

@Data
public class IssueTypeDonePercentageDTO {
    private String issueType;
    private Double donePercentage;
    private Long timeEstimate;
    private Long timeSpent;

    public IssueTypeDonePercentageDTO(String issueType, Number donePercentage, Long timeEstimate, Long timeSpent) {
        this.issueType = issueType;
        this.donePercentage = donePercentage != null ? donePercentage.doubleValue() : null;
        this.timeEstimate = timeEstimate;
        this.timeSpent = timeSpent;
    }
}
