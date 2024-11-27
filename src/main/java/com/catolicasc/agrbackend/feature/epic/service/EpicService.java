package com.catolicasc.agrbackend.feature.epic.service;

import com.catolicasc.agrbackend.clients.jira.dto.JiraIssueResponseDTO;
import com.catolicasc.agrbackend.feature.epic.domain.Epic;
import com.catolicasc.agrbackend.feature.epic.dto.EpicDTO;
import com.catolicasc.agrbackend.feature.epic.dto.EpicUsageDTO;
import com.catolicasc.agrbackend.feature.epic.repository.EpicRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EpicService {

    private final EpicRepository epicRepository;

    public EpicService(EpicRepository epicRepository) {
        this.epicRepository = epicRepository;
    }

    /**
     * Converte um EpicDTO para um Epic
     * @param epicResponseDTO Objeto retornado pela API do Jira
     * @return EpicDTO convertido
     */
    public EpicDTO toDto(JiraIssueResponseDTO.Epic epicResponseDTO) {
        EpicDTO epicDTO = new EpicDTO();
        epicDTO.setId(Long.parseLong(epicResponseDTO.getId()));
        epicDTO.setName(epicResponseDTO.getName());
        epicDTO.setSummary(epicResponseDTO.getSummary());
        epicDTO.setKey(epicResponseDTO.getKey());
        return epicDTO;
    }

    /**
     * Busca um épico no banco de dados, caso não exista, cria um novo
     * @param epicDTO
     * @return
     */
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

    public List<EpicUsageDTO> getEpicUsageByVersions(List<Long> versionIds) {
        return epicRepository.findEpicUsagesByVersions(versionIds);
    }

}
