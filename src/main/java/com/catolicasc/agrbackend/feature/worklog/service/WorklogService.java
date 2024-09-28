package com.catolicasc.agrbackend.feature.worklog.service;

import com.catolicasc.agrbackend.clients.jira.dto.JiraIssueResponseDTO;
import com.catolicasc.agrbackend.feature.worklog.domain.Worklog;
import com.catolicasc.agrbackend.feature.worklog.dto.WorklogDTO;
import com.catolicasc.agrbackend.feature.worklog.repository.WorklogRepository;
import com.catolicasc.agrbackend.feature.worklogentry.domain.WorklogEntry;
import com.catolicasc.agrbackend.feature.worklogentry.dto.WorklogEntryDTO;
import com.catolicasc.agrbackend.feature.worklogentry.service.WorkLogEntryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WorklogService {

    private final WorklogRepository worklogRepository;
    private final WorkLogEntryService workLogEntryService;

    public WorklogService(WorklogRepository worklogRepository, WorkLogEntryService workLogEntryService) {
        this.worklogRepository = worklogRepository;
        this.workLogEntryService = workLogEntryService;
    }

    public WorklogDTO toDTO(JiraIssueResponseDTO.Worklog worklog) {
        WorklogDTO worklogDTO = new WorklogDTO();
        worklogDTO.setTotal(worklog.getTotal());
        worklogDTO.setMaxResults(worklog.getMaxResults());
        worklogDTO.setStartAt(worklog.getStartAt());

        List<WorklogEntryDTO> worklogEntriesDTO = workLogEntryService.toDTOS(worklog.getWorklogs());

        worklogDTO.setWorklogEntries(worklogEntriesDTO);

        Worklog worklogDomain = toDomain(worklogDTO);
        Worklog worklog1 = worklogRepository.save(worklogDomain);

        worklogDTO.setId(worklog1.getId());

        return worklogDTO;
    }


    public Worklog toDomain(WorklogDTO worklogDTO) {
        Worklog worklog = new Worklog();
        worklog.setId(worklogDTO.getId());
        worklog.setTotal(worklogDTO.getTotal());
        worklog.setMaxResults(worklogDTO.getMaxResults());
        worklog.setStartAt(worklogDTO.getStartAt());
        List<WorklogEntry> worklogEntries = workLogEntryService.toDomain(worklogDTO.getWorklogEntries());
        worklog.setWorklogEntries(worklogEntries);
        return worklog;
    }
}
