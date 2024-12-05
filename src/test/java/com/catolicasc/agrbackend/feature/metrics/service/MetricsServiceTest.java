package com.catolicasc.agrbackend.feature.metrics.service;

import com.catolicasc.agrbackend.clients.prediction.dto.PredictVersionDelayDTO;
import com.catolicasc.agrbackend.clients.prediction.service.PredictionAPI;
import com.catolicasc.agrbackend.feature.epic.repository.EpicRepository;
import com.catolicasc.agrbackend.feature.issue.dto.IssuePlanningDTO;
import com.catolicasc.agrbackend.feature.issue.dto.IssueTypeDonePercentageDTO;
import com.catolicasc.agrbackend.feature.issue.repository.IssueRepository;
import com.catolicasc.agrbackend.feature.metrics.dto.CriticalIssueRelationDTO;
import com.catolicasc.agrbackend.feature.metrics.dto.DelieveryPercentagePerIssueTypeDTO;
import com.catolicasc.agrbackend.feature.metrics.dto.PredictDelayPerVersionDTO;
import com.catolicasc.agrbackend.feature.metrics.dto.WorkPlanningVarianceDTO;
import com.catolicasc.agrbackend.feature.sprint.dto.SprintDTO;
import com.catolicasc.agrbackend.feature.sprint.service.SprintService;
import com.catolicasc.agrbackend.feature.version.dto.VersionDTO;
import com.catolicasc.agrbackend.feature.version.service.VersionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MetricsServiceTest {

    @InjectMocks
    private MetricsService metricsService;
    @Mock
    private IssueRepository issueRepository;
    @Mock
    private VersionService versionService;
    @Mock
    private SprintService sprintService;
    @Mock
    private PredictionAPI predictionAPI;
    @Mock
    private EpicRepository epicRepository;

    @Test
    public void testGetDeliveryPercentagePerIssueTypeWithVersionId() {
        Long versionId = 1L;
        Long sprintId = null;

        // Mocking the repository response
        IssueTypeDonePercentageDTO issueTypeDTO = new IssueTypeDonePercentageDTO("Bug", 80.0, 0L, 0L);
        when(issueRepository.findDonePercentageByIssueType(versionId, sprintId))
                .thenReturn(List.of(issueTypeDTO));

        VersionDTO versionDTO = new VersionDTO();
        versionDTO.setId(versionId);
        versionDTO.setName("Version 1");
        when(versionService.getVersionDTO(versionId)).thenReturn(versionDTO);

        // Call the service method
        DelieveryPercentagePerIssueTypeDTO result = metricsService.getDelieveryPercentagePerIssueType(versionId, sprintId);

        // Validating the result
        assertNotNull(result);
        assertEquals(1, result.getDelieveryPercentagePerIssueType().size());
        assertEquals("Bug", result.getDelieveryPercentagePerIssueType().get(0).getIssueType());
        assertEquals(80.0, result.getDelieveryPercentagePerIssueType().get(0).getDonePercentage());
        assertEquals(versionDTO, result.getVersion());
    }

    @Test
    public void testGetDeliveryPercentagePerIssueTypeWithSprintId() {
        Long versionId = null;
        Long sprintId = 2L;

        // Mocking the repository response
        IssueTypeDonePercentageDTO issueTypeDTO = new IssueTypeDonePercentageDTO("Bug", 90.0, 0L, 0L);
        when(issueRepository.findDonePercentageByIssueType(versionId, sprintId))
                .thenReturn(List.of(issueTypeDTO));

        SprintDTO sprintDTO = new SprintDTO();
        sprintDTO.setId(sprintId);
        sprintDTO.setName("Sprint 1");
        when(sprintService.getSprintDTO(sprintId)).thenReturn(sprintDTO);

        // Call the service method
        DelieveryPercentagePerIssueTypeDTO result = metricsService.getDelieveryPercentagePerIssueType(versionId, sprintId);

        // Validating the result
        assertNotNull(result);
        assertEquals(1, result.getDelieveryPercentagePerIssueType().size());
        assertEquals("Bug", result.getDelieveryPercentagePerIssueType().get(0).getIssueType());
        assertEquals(90.0, result.getDelieveryPercentagePerIssueType().get(0).getDonePercentage());
        assertEquals(sprintDTO, result.getSprint());
    }

    @Test
    public void testGetWorkPlanningVariance() {
        Long versionId = 1L;
        Long sprintId = null;

        // Mocking the repository response
        IssuePlanningDTO issuePlanningDTO = new IssuePlanningDTO(20L, 10L, 30L, 400000L, 450000L, 500000L, 550000L);
        when(issueRepository.findWorkPlanningVariance(versionId, sprintId)).thenReturn(issuePlanningDTO);

        // Call the service method
        WorkPlanningVarianceDTO result = metricsService.getWorkPlanningVariance(versionId, sprintId);

        // Validating the result
        assertNotNull(result);
        assertEquals(issuePlanningDTO, result.getIssuePlanning());
    }

    @Test
    public void testGetDelayPrediction() {
        List<Long> versionIds = List.of(1L, 2L);

        // Mocking the external API response
        PredictVersionDelayDTO predictVersionDelayDTO = new PredictVersionDelayDTO();
        predictVersionDelayDTO.setDelayRiskPercentage(15.0);
        predictVersionDelayDTO.setVersionId(1L);
        when(predictionAPI.predictDelay("1,2")).thenReturn(List.of(predictVersionDelayDTO));

        // Mocking the versionService
        VersionDTO versionDTO1 = new VersionDTO();
        versionDTO1.setId(1L);
        versionDTO1.setName("Version 1");
        when(versionService.getVersionDTO(1L)).thenReturn(versionDTO1);

        // Call the service method
        List<PredictDelayPerVersionDTO> result = metricsService.getDelayPrediction(versionIds);

        // Validating the result
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(15.0, result.get(0).getDelayRiskPercentage());
        assertEquals("Version 1", result.get(0).getVersion().getName());
    }

    @Test
    public void testGetCriticalIssuesRelationByVersion() {
        List<Long> versionIds = List.of(1L);

        // Mocking the repository response
        com.catolicasc.agrbackend.feature.issue.dto.CriticalIssueRelationDTO criticalIssueRelationDTO = new com.catolicasc.agrbackend.feature.issue.dto.CriticalIssueRelationDTO(1L, 3L, 1L, 4L, 75.0, 25.0);

        when(issueRepository.findCriticalIssuesByVersions(versionIds)).thenReturn(List.of(criticalIssueRelationDTO));

        // Mocking the versionService
        VersionDTO versionDTO = new VersionDTO();
        versionDTO.setId(1L);
        versionDTO.setName("Version 1");
        when(versionService.getVersionDTO(1L)).thenReturn(versionDTO);

        // Call the service method
        List<CriticalIssueRelationDTO> result = metricsService.getCriticalIssuesRelation(versionIds, null);

        // Validating the result
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Version 1", result.get(0).getVersion().getName());
        assertEquals(75.0, result.get(0).getCriticalIssueRelation().get(0).getCriticalIssuesPercentage());
    }

}
