package com.catolicasc.agrbackend.feature.issue.dto;

import lombok.Data;

@Data
public class BugIssuesRelationDTO {
    private Long id; // Identificador da versão ou sprint
    private Long bugIssuesCount;
    private Long nonBugIssuesCount;
    private Long totalIssuesCount;
    private Double bugIssuesPercentage;
    private Double nonBugIssuesPercentage;

    public BugIssuesRelationDTO(Long id, Long bugIssuesCount, Long nonBugIssuesCount, Long totalIssuesCount,
                                Double bugIssuesPercentage, Double nonBugIssuesPercentage) {
        this.id = id;
        this.bugIssuesCount = bugIssuesCount;
        this.nonBugIssuesCount = nonBugIssuesCount;
        this.totalIssuesCount = totalIssuesCount;
        this.bugIssuesPercentage = bugIssuesPercentage;
        this.nonBugIssuesPercentage = nonBugIssuesPercentage;
    }
}
