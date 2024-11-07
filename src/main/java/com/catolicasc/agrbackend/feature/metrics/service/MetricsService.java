package com.catolicasc.agrbackend.feature.metrics.service;

import com.catolicasc.agrbackend.clients.prediction.dto.PredictVersionDelayDTO;
import com.catolicasc.agrbackend.clients.prediction.service.PredictionAPI;
import com.catolicasc.agrbackend.feature.issue.dto.IssuePlanningDTO;
import com.catolicasc.agrbackend.feature.issue.dto.IssueTypeDonePercentageDTO;
import com.catolicasc.agrbackend.feature.issue.repository.IssueRepository;
import com.catolicasc.agrbackend.feature.metrics.dto.DelieveryPercentagePerIssueTypeDTO;
import com.catolicasc.agrbackend.feature.metrics.dto.PredictDelayPerVersionDTO;
import com.catolicasc.agrbackend.feature.metrics.dto.WorkPlanningVarianceDTO;
import com.catolicasc.agrbackend.feature.sprint.service.SprintService;
import com.catolicasc.agrbackend.feature.version.service.VersionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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

    //Clients
    private final PredictionAPI predictionAPI;

    public MetricsService(IssueRepository issueRepository, VersionService versionService, SprintService sprintService, PredictionAPI predictionAPI) {
        this.issueRepository = issueRepository;
        this.versionService = versionService;
        this.sprintService = sprintService;
        this.predictionAPI = predictionAPI;
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

    public WorkPlanningVarianceDTO getWorkPlanningVariance(Long versionId, Long sprintId) {
        if (isNull(versionId) && isNull(sprintId)) {
            log.info("VersionId and SprintId are null");
        }

        IssuePlanningDTO workPlanningVarianceDTO = issueRepository.findWorkPlanningVariance(versionId, sprintId);

        WorkPlanningVarianceDTO workPlanningVariance = new WorkPlanningVarianceDTO();
        workPlanningVariance.setIssuePlanning(workPlanningVarianceDTO);
        if (!isNull(versionId)) {
            workPlanningVariance.setVersion(versionService.getVersionDTO(versionId));
        }
        if (!isNull(sprintId)) {
            workPlanningVariance.setSprint(sprintService.getSprintDTO(sprintId));
        }

        return workPlanningVariance;
    }

    public List<PredictDelayPerVersionDTO> getDelayPrediction(List<Long> versionIds) {
        String versionIdsString = String.join(",", versionIds.toString().replace("[", "").replace("]", "").split(", "));
        List<PredictVersionDelayDTO> predictVersionDelayDTOS = predictionAPI.predictDelay(versionIdsString);
        List<PredictDelayPerVersionDTO> predictDelayPerVersionDTOS = new ArrayList<>();
        for (PredictVersionDelayDTO predictVersionDelayDTO : predictVersionDelayDTOS) {
            PredictDelayPerVersionDTO predictDelayPerVersionDTO = new PredictDelayPerVersionDTO();
            predictDelayPerVersionDTO.setVersion(versionService.getVersionDTO(predictVersionDelayDTO.getVersionId()));
            predictDelayPerVersionDTO.setDelayRiskPercentage(predictVersionDelayDTO.getDelayRiskPercentage());
            predictDelayPerVersionDTOS.add(predictDelayPerVersionDTO);
        }

        return predictDelayPerVersionDTOS;
    }
}
