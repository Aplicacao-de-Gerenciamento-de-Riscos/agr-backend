package com.catolicasc.agrbackend.feature.metrics.dto;

import com.catolicasc.agrbackend.feature.issue.dto.BugIssuesRelationDTO;
import com.catolicasc.agrbackend.feature.sprint.dto.SprintDTO;
import com.catolicasc.agrbackend.feature.version.dto.VersionDTO;
import lombok.Data;

import java.util.List;

@Data
public class BugIssueRelationDTO {
    private VersionDTO version;
    private SprintDTO sprint;
    private List<BugIssuesRelationDTO> bugIssuesRelation;
}
