package com.catolicasc.agrbackend.feature.metrics.dto;

import com.catolicasc.agrbackend.feature.issue.dto.IssuePlanningDTO;
import com.catolicasc.agrbackend.feature.sprint.dto.SprintDTO;
import com.catolicasc.agrbackend.feature.version.dto.VersionDTO;
import lombok.Data;

@Data
public class WorkPlanningVarianceDTO {
    private SprintDTO sprint;
    private VersionDTO version;
    private IssuePlanningDTO issuePlanning;
}
