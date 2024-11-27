package com.catolicasc.agrbackend.feature.metrics.dto;

import com.catolicasc.agrbackend.feature.epic.dto.EpicUsageDTO;
import com.catolicasc.agrbackend.feature.version.dto.VersionDTO;
import lombok.Data;

import java.util.List;

@Data
public class EpicPerVersionDTO {
    private VersionDTO version;
    private List<EpicUsageDTO> epicsUsage;
}
