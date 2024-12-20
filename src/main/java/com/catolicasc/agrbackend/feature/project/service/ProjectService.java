package com.catolicasc.agrbackend.feature.project.service;

import com.catolicasc.agrbackend.feature.project.domain.Project;
import com.catolicasc.agrbackend.feature.project.dto.ProjectDTO;
import com.catolicasc.agrbackend.feature.project.repository.ProjectRepository;
import com.fasterxml.jackson.databind.util.BeanUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    /**
     * Busca todos os projetos cadastrados no banco de dados
     *
     * @return Lista de projetos
     */
    public List<Project> findAll() {
        return projectRepository.findAll();
    }

    /**
     * Busca um projeto no banco de dados
     *
     * @param id Identificador do projeto
     * @return Projeto
     */
    public Project findById(Long id) {
        return projectRepository.findById(id).orElse(null);
    }

    /**
     * Transforma um domain Project em um DTO Project
     *
     * @param project Projeto
     * @return ProjectDTO
     */
    public ProjectDTO toDTO(Project project) {
        ProjectDTO projectDTO = new ProjectDTO();
        BeanUtils.copyProperties(project, projectDTO);
        return projectDTO;
    }

}
