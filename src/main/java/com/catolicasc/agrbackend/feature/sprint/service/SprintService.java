package com.catolicasc.agrbackend.feature.sprint.service;

import com.catolicasc.agrbackend.clients.jira.dto.JiraIssueResponseDTO;
import com.catolicasc.agrbackend.feature.sprint.domain.Sprint;
import com.catolicasc.agrbackend.feature.sprint.dto.SprintDTO;
import com.catolicasc.agrbackend.feature.sprint.repository.SprintRepository;
import org.springframework.stereotype.Service;

@Service
public class SprintService {

    private final SprintRepository sprintRepository;

    public SprintService(
        SprintRepository sprintRepository
    ) {
        this.sprintRepository = sprintRepository;
    }

    public Sprint findById(Long id) {
        return sprintRepository.findById(id).orElse(null);
    }

    public Sprint toDomain(JiraIssueResponseDTO.Sprint sprint) {
        Sprint sprintDomain = new Sprint();
        sprintDomain.setId(Long.parseLong(sprint.getId()));
        sprintDomain.setName(sprint.getName());
        return sprintDomain;
    }

    public Sprint toDomain(SprintDTO sprintDTO) {
        Sprint sprint = new Sprint();
        sprint.setId(sprintDTO.getId());
        sprint.setName(sprintDTO.getName());
        return sprint;
    }

    public SprintDTO toDto(JiraIssueResponseDTO.Sprint sprint) {
        Sprint sprintDomain = findById(Long.parseLong(sprint.getId()));
        SprintDTO sprintDTO = new SprintDTO();
        if (sprintDomain != null) {
            sprintDTO.setId(sprintDomain.getId());
            sprintDTO.setName(sprintDomain.getName());
        } else {
            Sprint sprintDomain1 = toDomain(sprint);
            Sprint sprint1 = sprintRepository.save(sprintDomain1);
            sprintDTO.setId(sprint1.getId());
            sprintDTO.setName(sprint1.getName());
        }

        return sprintDTO;
    }

}
