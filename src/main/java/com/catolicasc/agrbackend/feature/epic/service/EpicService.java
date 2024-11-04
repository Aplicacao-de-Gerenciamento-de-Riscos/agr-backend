package com.catolicasc.agrbackend.feature.epic.service;

import com.catolicasc.agrbackend.clients.jira.dto.JiraIssueResponseDTO;
import com.catolicasc.agrbackend.feature.epic.domain.Epic;
import com.catolicasc.agrbackend.feature.epic.dto.EpicDTO;
import com.catolicasc.agrbackend.feature.epic.repository.EpicRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class EpicService {

    private final EpicRepository epicRepository;

    public EpicService(EpicRepository epicRepository) {
        this.epicRepository = epicRepository;
    }

    public Epic findById(Long id) {
        return epicRepository.findById(id).orElse(null);
    }

    public Epic toDomain(EpicDTO epicDTO) {
        Epic epic = new Epic();
        epic.setId(epicDTO.getId());
        epic.setName(epicDTO.getName());
        epic.setSummary(epicDTO.getSummary());
        epic.setKey(epicDTO.getKey());
        return epic;
    }

    public Epic toDomain(JiraIssueResponseDTO.Epic epic) {
        Epic existingEpic = findById(Long.parseLong(epic.getId()));
        if (existingEpic != null) {
            return existingEpic;
        }

        Epic epicDomain = new Epic();
        epicDomain.setId(Long.parseLong(epic.getId()));
        epicDomain.setName(epic.getName());
        return epicDomain;
    }

    public EpicDTO toDto(JiraIssueResponseDTO.Epic epicResponseDTO) {
        EpicDTO epicDTO = new EpicDTO();
        epicDTO.setId(Long.parseLong(epicResponseDTO.getId()));
        epicDTO.setName(epicResponseDTO.getName());
        epicDTO.setSummary(epicResponseDTO.getSummary());
        epicDTO.setKey(epicResponseDTO.getKey());
        return epicDTO;
    }

    @Transactional
    public Epic findOrCreateEpic(EpicDTO epicDTO) {
        Long epicId = epicDTO.getId();
        Epic epic = epicRepository.findById(epicId).orElse(null);
        if (epic == null) {
            epic = new Epic();
            epic.setId(epicId);
            epic.setName(epicDTO.getName());
            epic.setSummary(epicDTO.getSummary());
            epic.setKey(epicDTO.getKey());
            epic = epicRepository.save(epic);
        }
        return epic;
    }

}
