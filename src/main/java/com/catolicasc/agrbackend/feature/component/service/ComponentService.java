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

    /**
     * Converte um objeto do Jira para um ComponentDTO
     * @param component Objeto retornado pela API do Jira
     * @return ComponentDTO convertido
     */
    public ComponentDTO toDto(JiraIssueResponseDTO.Component component) {
        ComponentDTO componentDTO = new ComponentDTO();
        componentDTO.setId(Long.parseLong(component.getId()));
        componentDTO.setName(component.getName());
        return componentDTO;
    }

    /**
     * Busca um componente no banco de dados, caso não exista, cria um novo
     * @param componentDTO
     * @return
     */
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
