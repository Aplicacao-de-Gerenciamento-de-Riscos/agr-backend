package com.catolicasc.agrbackend.feature.worklog.dto;

import com.catolicasc.agrbackend.feature.worklogentry.dto.WorklogEntryDTO;
import lombok.Data;

import java.util.List;

@Data
public class WorklogDTO {
    private Long id;
    private Integer startAt;
    private Integer maxResults;
    private Integer total;
    private List<WorklogEntryDTO> worklogEntries;
}
