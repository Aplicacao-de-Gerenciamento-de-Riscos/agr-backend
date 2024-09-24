package com.catolicasc.agrbackend.feature.component.service;

import com.catolicasc.agrbackend.clients.jira.dto.JiraIssueResponseDTO;
import com.catolicasc.agrbackend.feature.component.domain.Component;
import com.catolicasc.agrbackend.feature.component.dto.ComponentDTO;
import com.catolicasc.agrbackend.feature.component.repository.ComponentRepository;
import org.springframework.stereotype.Service;

@Service
public class ComponentService {

    private final ComponentRepository componentRepository;

    public ComponentService(
        ComponentRepository componentRepository
    ) {
        this.componentRepository = componentRepository;
    }

    public ComponentDTO toDto(JiraIssueResponseDTO.Component component) {
        ComponentDTO componentDTO = new ComponentDTO();
        Component componentDomain = findById(Long.parseLong(component.getId()));
        if (componentDomain != null) {
            componentDTO.setId(componentDomain.getId());
            componentDTO.setName(componentDomain.getName());
        } else {
            Component component1 = componentRepository.save(toDomain(component));
            componentDTO.setId(component1.getId());
            componentDTO.setName(component1.getName());
        }
        return componentDTO;
    }

    public Component toDomain(JiraIssueResponseDTO.Component component) {
        Component componentDomain = new Component();
        component.setId(component.getId());
        component.setName(component.getName());
        return componentDomain;
    }

    public Component findById(Long id) {
        return componentRepository.findById(id).orElse(null);
    }
}
