package com.catolicasc.agrbackend.feature.worklogentry.service;

import com.catolicasc.agrbackend.clients.jira.dto.JiraIssueResponseDTO;
import com.catolicasc.agrbackend.feature.worklog.domain.Worklog;
import com.catolicasc.agrbackend.feature.worklog.dto.WorklogDTO;
import com.catolicasc.agrbackend.feature.worklog.service.WorklogService;
import com.catolicasc.agrbackend.feature.worklogentry.domain.WorklogEntry;
import com.catolicasc.agrbackend.feature.worklogentry.dto.WorklogEntryDTO;
import com.catolicasc.agrbackend.feature.worklogentry.repository.WorklogEntryRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.nonNull;

@Service
@Transactional
public class WorkLogEntryService {

    private static final Logger log = LoggerFactory.getLogger(WorkLogEntryService.class);
    private final WorklogEntryRepository worklogEntryRepository;
    private final WorklogService worklogService;

    public WorkLogEntryService(WorklogEntryRepository worklogEntryRepository, @Lazy WorklogService worklogService) {
        this.worklogService = worklogService;
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

    public void syncWorklogEntries(Worklog worklog, WorklogDTO worklogDTO) {
        if (nonNull(worklog)) {
            // Deletar todas as worklog entries do worklog
            worklogEntryRepository.deleteAllByWorklog(worklog);
            // Converte as worklog entries
            List<WorklogEntry> worklogEntries = toDomain(worklogDTO.getWorklogEntries());
            // Seta o worklog para as worklog entries
            worklogEntries.forEach(worklogEntry -> worklogEntry.setWorklog(worklog));
            // Salvar as novas worklog entries
            worklogEntryRepository.saveAll(worklogEntries);
        } else {
            log.info("Worklog não encontrado");
        }
    }

    public void saveAll(List<WorklogEntry> worklogEntries) {
        worklogEntryRepository.saveAll(worklogEntries);
    }
}
