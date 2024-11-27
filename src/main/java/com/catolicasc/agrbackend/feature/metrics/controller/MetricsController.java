package com.catolicasc.agrbackend.feature.metrics.controller;

import com.catolicasc.agrbackend.feature.issue.dto.BugIssuesRelationDTO;
import com.catolicasc.agrbackend.feature.metrics.dto.*;
import com.catolicasc.agrbackend.feature.metrics.service.MetricsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.nonNull;

@RestController
@RequestMapping("v1/metrics")
public class MetricsController {

    @Autowired
    private MetricsService metricsService;

    @GetMapping("/delivery-percentage")
    public ResponseEntity<List<DelieveryPercentagePerIssueTypeDTO>> getDeliveryPercentagePerIssueType(@RequestParam(name = "versionIds", required = false) List<Long> versionIds, @RequestParam(name = "sprintIds", required = false) List<Long> sprintIds) {
        List<DelieveryPercentagePerIssueTypeDTO> delieveryPercentagePerIssueTypeDTOS = new ArrayList<>();
        if (nonNull(sprintIds) && !sprintIds.isEmpty()) {
            for (Long sprintId : sprintIds) {
                delieveryPercentagePerIssueTypeDTOS.add(metricsService.getDelieveryPercentagePerIssueType(null, sprintId));
            }
        } else if (nonNull(versionIds) && !versionIds.isEmpty()) {
            for (Long versionId : versionIds) {
                delieveryPercentagePerIssueTypeDTOS.add(metricsService.getDelieveryPercentagePerIssueType(versionId, null));
            }
        } else {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(delieveryPercentagePerIssueTypeDTOS);
    }

    @GetMapping("/work-planning-variance")
    public ResponseEntity<List<WorkPlanningVarianceDTO>> getWorkPlanningVariance(@RequestParam(name = "versionIds", required = false) List<Long> versionIds, @RequestParam(name = "sprintIds", required = false) List<Long> sprintIds) {
        List<WorkPlanningVarianceDTO> workPlanningVarianceDTOS = new ArrayList<>();
        if (nonNull(sprintIds) && !sprintIds.isEmpty()) {
            for (Long sprintId : sprintIds) {
                workPlanningVarianceDTOS.add(metricsService.getWorkPlanningVariance(null, sprintId));
            }
        } else if (nonNull(versionIds) && !versionIds.isEmpty()) {
            for (Long versionId : versionIds) {
                workPlanningVarianceDTOS.add(metricsService.getWorkPlanningVariance(versionId, null));
            }
        } else {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(workPlanningVarianceDTOS);
    }

    @GetMapping("/predict-delay")
    public ResponseEntity<List<PredictDelayPerVersionDTO>> predictDelay(@RequestParam(name = "versionIds") List<Long> versionIds) {
        return ResponseEntity.ok(metricsService.getDelayPrediction(versionIds));
    }

    @GetMapping("/critical-issues-relation")
    public ResponseEntity<List<CriticalIssueRelationDTO>> getCriticalIssuesRelation(@RequestParam(name = "versionIds", required = false) List<Long> versionIds, @RequestParam(name = "sprintIds", required = false) List<Long> sprintIds) {
        if (nonNull(sprintIds) && !sprintIds.isEmpty()) {
            return ResponseEntity.ok(metricsService.getCriticalIssuesRelation(null, sprintIds));
        } else if (nonNull(versionIds) && !versionIds.isEmpty()) {
            return ResponseEntity.ok(metricsService.getCriticalIssuesRelation(versionIds, null));
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/bug-issues-relation")
    public ResponseEntity<List<BugIssueRelationDTO>> getBugIssuesRelation(@RequestParam(name = "versionIds", required = false) List<Long> versionIds, @RequestParam(name = "sprintIds", required = false) List<Long> sprintIds) {
        if (nonNull(sprintIds) && !sprintIds.isEmpty()) {
            return ResponseEntity.ok(metricsService.getBugIssuesRelation(null, sprintIds));
        } else if (nonNull(versionIds) && !versionIds.isEmpty()) {
            return ResponseEntity.ok(metricsService.getBugIssuesRelation(versionIds, null));
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/epics-per-version")
    public ResponseEntity<List<EpicPerVersionDTO>> getEpicsPerVersion(@RequestParam(name = "versionIds", required = false) List<Long> versionIds) {
        if (nonNull(versionIds) && !versionIds.isEmpty()) {
            return ResponseEntity.ok(metricsService.getEpicsPerVersion(versionIds));
        } else {
            return ResponseEntity.badRequest().build();
        }
    }
}
