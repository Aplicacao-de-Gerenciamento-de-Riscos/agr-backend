package com.catolicasc.agrbackend.feature.metrics.dto;

import com.catolicasc.agrbackend.feature.issue.dto.IssueTypeDonePercentageDTO;
import com.catolicasc.agrbackend.feature.sprint.dto.SprintDTO;
import com.catolicasc.agrbackend.feature.version.dto.VersionDTO;
import lombok.Data;

import java.util.List;

@Data
public class DelieveryPercentagePerIssueTypeDTO {
    private List<IssueTypeDonePercentageDTO> delieveryPercentagePerIssueType;
    private VersionDTO version;
    private SprintDTO sprint;
}
