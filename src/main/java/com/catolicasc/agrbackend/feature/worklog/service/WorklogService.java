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

import static java.util.Objects.nonNull;

@Service
public class WorklogService {

    private final WorklogRepository worklogRepository;
    private final WorkLogEntryService workLogEntryService;

    public WorklogService(WorklogRepository worklogRepository, WorkLogEntryService workLogEntryService) {
        this.worklogRepository = worklogRepository;
        this.workLogEntryService = workLogEntryService;
    }

    public Worklog findById(Long id) {
        return worklogRepository.findById(id).orElse(null);
    }

    public WorklogDTO toDTO(JiraIssueResponseDTO.Worklog worklog) {
        WorklogDTO worklogDTO = new WorklogDTO();
        worklogDTO.setTotal(worklog.getTotal());
        worklogDTO.setMaxResults(worklog.getMaxResults());
        worklogDTO.setStartAt(worklog.getStartAt());

        // Converte os Worklog Entries
        List<WorklogEntryDTO> worklogEntriesDTO = workLogEntryService.toDTOS(worklog.getWorklogs());
        worklogDTO.setWorklogEntries(worklogEntriesDTO);

        return worklogDTO;
    }

    public Worklog toDomain(WorklogDTO worklogDTO) {
        Worklog worklog = new Worklog();
        worklog.setTotal(worklogDTO.getTotal());
        worklog.setMaxResults(worklogDTO.getMaxResults());
        worklog.setStartAt(worklogDTO.getStartAt());
        return worklog;
    }

    public Worklog save(Worklog worklog) {
        return worklogRepository.save(worklog);
    }
}
