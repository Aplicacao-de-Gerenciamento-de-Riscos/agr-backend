package com.catolicasc.agrbackend.feature.worklogentry.service;

import com.catolicasc.agrbackend.clients.jira.dto.JiraIssueResponseDTO;
import com.catolicasc.agrbackend.feature.worklogentry.domain.WorklogEntry;
import com.catolicasc.agrbackend.feature.worklogentry.dto.WorklogEntryDTO;
import com.catolicasc.agrbackend.feature.worklogentry.repository.WorklogEntryRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class WorkLogEntryService {

    private final WorklogEntryRepository worklogEntryRepository;

    public WorkLogEntryService(WorklogEntryRepository worklogEntryRepository) {
        this.worklogEntryRepository = worklogEntryRepository;
    }

    public List<WorklogEntryDTO> toDTOS(List<JiraIssueResponseDTO.WorklogEntry> worklogEntries) {
        List<WorklogEntryDTO> worklogEntryDTOS = new ArrayList<>();

        worklogEntries.forEach(worklogEntry -> {
            WorklogEntryDTO worklogEntryDTO = new WorklogEntryDTO();
            worklogEntryDTO.setAuthor(worklogEntry.getAuthor().getEmailAddress());
            worklogEntryDTO.setSelf(worklogEntry.getSelf());
            worklogEntryDTO.setCreated(worklogEntry.getCreated());
            worklogEntryDTO.setUpdated(worklogEntry.getUpdated());
            worklogEntryDTO.setTimeSpent(worklogEntry.getTimeSpent());

            worklogEntryDTOS.add(worklogEntryDTO);
        });

        List<WorklogEntry> worklogEntriesDomain = toDomain(worklogEntryDTOS);
        worklogEntryRepository.saveAll(worklogEntriesDomain);

        return worklogEntryDTOS;
    }

    public List<WorklogEntry> toDomain(List<WorklogEntryDTO> worklogEntryDTOS) {
        List<WorklogEntry> worklogEntries = new ArrayList<>();
        worklogEntryDTOS.forEach(worklogEntryDTO -> {
            WorklogEntry worklogEntry = new WorklogEntry();
            worklogEntry.setAuthor(worklogEntryDTO.getAuthor());
            worklogEntry.setSelf(worklogEntryDTO.getSelf());
            worklogEntry.setCreated(worklogEntryDTO.getCreated());
            worklogEntry.setUpdated(worklogEntryDTO.getUpdated());
            worklogEntry.setTimeSpent(worklogEntryDTO.getTimeSpent());
            worklogEntries.add(worklogEntry);
        });
        return worklogEntries;
    }
}
