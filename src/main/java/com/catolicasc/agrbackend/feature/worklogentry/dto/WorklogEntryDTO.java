package com.catolicasc.agrbackend.feature.worklogentry.dto;

import lombok.Data;

@Data
public class WorklogEntryDTO {
    private Long id;
    private String self;
    private String author;
    private String created;
    private String updated;
    private String timeSpent;
}
