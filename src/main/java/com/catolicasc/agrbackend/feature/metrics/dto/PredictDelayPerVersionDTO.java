package com.catolicasc.agrbackend.feature.metrics.dto;

import com.catolicasc.agrbackend.feature.version.dto.VersionDTO;
import lombok.Data;

@Data
public class PredictDelayPerVersionDTO {
    private VersionDTO version;
    private Double delayRiskPercentage;
}
