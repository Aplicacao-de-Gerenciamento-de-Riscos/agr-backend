package com.catolicasc.agrbackend.feature.epic.dto;

import lombok.Data;

@Data
public class EpicUsageDTO {
    private Long versionId;
    private Long id;
    private String name;
    private String summary;
    private String key;
    private Long timespent;

    public EpicUsageDTO(Long versionId, Long id, String name, String summary, String key, Long timespent) {
        this.versionId = versionId;
        this.id = id;
        this.name = name;
        this.summary = summary;
        this.key = key;
        this.timespent = timespent;
    }
}
