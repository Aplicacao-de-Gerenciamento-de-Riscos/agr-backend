package com.catolicasc.agrbackend.clients.prediction.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PredictVersionDelayDTO {
    @JsonProperty("delay_risk_percentage")
    private Double delayRiskPercentage;
    @JsonProperty("version_id")
    private Long versionId;
}