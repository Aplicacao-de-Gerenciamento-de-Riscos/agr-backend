package com.catolicasc.agrbackend.feature.metrics.controller;

import com.catolicasc.agrbackend.feature.metrics.dto.DelieveryPercentagePerIssueTypeDTO;
import com.catolicasc.agrbackend.feature.metrics.service.MetricsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("v1/metrics")
public class MetricsController {

    @Autowired
    private MetricsService metricsService;

    @GetMapping("/delivery-percentage")
    public ResponseEntity<DelieveryPercentagePerIssueTypeDTO> getDeliveryPercentagePerIssueType(@RequestParam(name = "versionId", required = false) Long versionId, @RequestParam(name = "sprintId",required = false) Long sprintId) {
        return ResponseEntity.ok(metricsService.getDelieveryPercentagePerIssueType(versionId, sprintId));
    }
}
