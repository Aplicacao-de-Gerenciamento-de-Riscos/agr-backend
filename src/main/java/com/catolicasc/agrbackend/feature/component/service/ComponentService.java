package com.catolicasc.agrbackend.feature.component.service;

import com.catolicasc.agrbackend.clients.jira.dto.JiraIssueResponseDTO;
import com.catolicasc.agrbackend.feature.component.domain.Component;
import com.catolicasc.agrbackend.feature.component.dto.ComponentDTO;
import com.catolicasc.agrbackend.feature.component.repository.ComponentRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class ComponentService {

    private final ComponentRepository componentRepository;

    public ComponentService(ComponentRepository componentRepository) {
        this.componentRepository = componentRepository;
    }

    public Component findById(Long id) {
        return componentRepository.findById(id).orElse(null);
    }

    public ComponentDTO toDto(JiraIssueResponseDTO.Component component) {
        ComponentDTO componentDTO = new ComponentDTO();
        componentDTO.setId(Long.parseLong(component.getId()));
        componentDTO.setName(component.getName());
        return componentDTO;
    }


    public Component toDomain(JiraIssueResponseDTO.Component component) {
        Component existingComponent = findById(Long.parseLong(component.getId()));
        if (existingComponent != null) {
            return existingComponent;
        }

        Component componentDomain = new Component();
        componentDomain.setId(Long.parseLong(component.getId()));
        componentDomain.setName(component.getName());
        return componentDomain;
    }

    public Component toDomain(ComponentDTO componentDTO) {
        Component component = new Component();
        component.setId(componentDTO.getId());
        component.setName(componentDTO.getName());
        return component;
    }

    @Transactional
    public Component findOrCreateComponent(ComponentDTO componentDTO) {
        Long componentId = componentDTO.getId();
        Component component = componentRepository.findById(componentId).orElse(null);
        if (component == null) {
            component = new Component();
            component.setId(componentId);
            component.setName(componentDTO.getName());
            component = componentRepository.save(component);
        }
        return component;
    }

}
