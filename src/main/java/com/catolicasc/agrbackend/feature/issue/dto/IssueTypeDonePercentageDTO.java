package com.catolicasc.agrbackend.feature.issue.dto;

import lombok.Data;

@Data
public class IssueTypeDonePercentageDTO {
    private String issueType;
    private Double donePercentage;

    public IssueTypeDonePercentageDTO(String issueType, Number donePercentage) {
        this.issueType = issueType;
        this.donePercentage = donePercentage != null ? donePercentage.doubleValue() : null;
    }
}
