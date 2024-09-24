package com.catolicasc.agrbackend.feature.epic.service;

import com.catolicasc.agrbackend.clients.jira.dto.JiraIssueResponseDTO;
import com.catolicasc.agrbackend.feature.epic.domain.Epic;
import com.catolicasc.agrbackend.feature.epic.dto.EpicDTO;
import com.catolicasc.agrbackend.feature.epic.repository.EpicRepository;
import org.springframework.stereotype.Service;

@Service
public class EpicService {

    private final EpicRepository epicRepository;

    public EpicService(
        EpicRepository epicRepository
    ) {
        this.epicRepository = epicRepository;
    }

    public Epic findById(Long id) {
        return epicRepository.findById(id).orElse(null);
    }

    public Epic toDomain(JiraIssueResponseDTO.Epic epic) {
        Epic epicDomain = new Epic();
        epic.setId(epic.getId());
        epic.setName(epic.getName());
        return epicDomain;
    }

    public EpicDTO toDto(JiraIssueResponseDTO.Epic epic) {
        Epic epicDomain = findById(Long.parseLong(epic.getId()));
        EpicDTO epicDTO = new EpicDTO();
        if (epicDomain != null) {
            epicDTO.setId(epicDomain.getId());
            epicDTO.setName(epicDomain.getName());
        } else {
            Epic epic1 = epicRepository.save(toDomain(epic));
            epicDTO.setId(epic1.getId());
            epicDTO.setName(epic1.getName());
        }

        return epicDTO;
    }
}
