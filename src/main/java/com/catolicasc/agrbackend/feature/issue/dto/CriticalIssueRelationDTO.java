package com.catolicasc.agrbackend.feature.issue.dto;

import lombok.Data;

@Data
public class CriticalIssueRelationDTO {
    private Long id; // ID (sprint ou version)
    private Long criticalIssuesCount; // Contagem de issues críticas
    private Long nonCriticalIssuesCount; // Contagem de issues não críticas
    private Long totalIssuesCount; // Total de issues
    private Double criticalIssuesPercentage; // Porcentagem de críticas
    private Double nonCriticalIssuesPercentage; // Porcentagem de não críticas

    public CriticalIssueRelationDTO(Long id, Long criticalIssuesCount, Long nonCriticalIssuesCount,
                                    Long totalIssuesCount, Double criticalIssuesPercentage,
                                    Double nonCriticalIssuesPercentage) {
        this.id = id;
        this.criticalIssuesCount = criticalIssuesCount;
        this.nonCriticalIssuesCount = nonCriticalIssuesCount;
        this.totalIssuesCount = totalIssuesCount;
        this.criticalIssuesPercentage = criticalIssuesPercentage;
        this.nonCriticalIssuesPercentage = nonCriticalIssuesPercentage;
    }
}
