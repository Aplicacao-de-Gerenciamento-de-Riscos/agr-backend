package com.catolicasc.agrbackend.feature.metrics.service;

import com.catolicasc.agrbackend.feature.issue.dto.IssueTypeDonePercentageDTO;
import com.catolicasc.agrbackend.feature.issue.repository.IssueRepository;
import com.catolicasc.agrbackend.feature.issue.service.IssueService;
import com.catolicasc.agrbackend.feature.metrics.dto.DelieveryPercentagePerIssueTypeDTO;
import com.catolicasc.agrbackend.feature.sprint.service.SprintService;
import com.catolicasc.agrbackend.feature.version.service.VersionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.Objects.isNull;

@Service
public class MetricsService {

    //Logger
    private static final Logger log = LoggerFactory.getLogger(MetricsService.class);

    //Repositories
    private final IssueRepository issueRepository;

    //Services
    private final VersionService versionService;
    private final SprintService sprintService;

    public MetricsService(IssueRepository issueRepository, VersionService versionService, SprintService sprintService) {
        this.issueRepository = issueRepository;
        this.versionService = versionService;
        this.sprintService = sprintService;
    }

    public DelieveryPercentagePerIssueTypeDTO getDelieveryPercentagePerIssueType(Long versionId, Long sprintId) {
        if (isNull(versionId) && isNull(sprintId)) {
            log.info("VersionId and SprintId are null");
        }

        List<IssueTypeDonePercentageDTO> issueTypeDonePercentageDTOS = issueRepository.findDonePercentageByIssueType(versionId, sprintId);

        DelieveryPercentagePerIssueTypeDTO delieveryPercentagePerIssueTypeDTO = new DelieveryPercentagePerIssueTypeDTO();
        delieveryPercentagePerIssueTypeDTO.setDelieveryPercentagePerIssueType(issueTypeDonePercentageDTOS);
        if (!isNull(versionId)) {
            delieveryPercentagePerIssueTypeDTO.setVersion(versionService.getVersionDTO(versionId));
        }
        if (!isNull(sprintId)) {
            delieveryPercentagePerIssueTypeDTO.setSprint(sprintService.getSprintDTO(sprintId));
        }

        return delieveryPercentagePerIssueTypeDTO;
    }
}
