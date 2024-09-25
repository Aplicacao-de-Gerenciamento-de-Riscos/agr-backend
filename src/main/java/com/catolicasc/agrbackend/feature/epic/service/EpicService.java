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
        epicDomain.setId(Long.parseLong(epic.getId()));
        epicDomain.setName(epic.getName());
        return epicDomain;
    }

    public Epic toDomain(EpicDTO epicDTO) {
        Epic epic = new Epic();
        epic.setId(epicDTO.getId());
        epic.setName(epicDTO.getName());
        return epic;
    }

    public EpicDTO toDto(JiraIssueResponseDTO.Epic epic) {
        Epic epicDomain = findById(Long.parseLong(epic.getId()));
        EpicDTO epicDTO = new EpicDTO();
        if (epicDomain != null) {
            epicDTO.setId(epicDomain.getId());
            epicDTO.setName(epicDomain.getName());
        } else {
            Epic epicDomain1 = toDomain(epic);
            Epic epic1 = epicRepository.save(epicDomain1);
            epicDTO.setId(epic1.getId());
            epicDTO.setName(epic1.getName());
        }

        return epicDTO;
    }
}
