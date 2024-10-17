package com.catolicasc.agrbackend.feature.version.service;

import com.catolicasc.agrbackend.clients.jira.dto.JiraVersionResponseDTO;
import com.catolicasc.agrbackend.clients.jira.service.JiraAPI;
import com.catolicasc.agrbackend.feature.project.domain.Project;
import com.catolicasc.agrbackend.feature.project.service.ProjectService;
import com.catolicasc.agrbackend.feature.version.domain.Version;
import com.catolicasc.agrbackend.feature.version.dto.VersionDTO;
import com.catolicasc.agrbackend.feature.version.repository.VersionRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

import static java.util.Objects.nonNull;

@Service
public class VersionService {

    private final VersionRepository versionRepository;
    private final JiraAPI jiraAPI;
    private final ProjectService projectService;

    public VersionService(VersionRepository versionRepository, JiraAPI jiraAPI, ProjectService projectService) {
        this.versionRepository = versionRepository;
        this.jiraAPI = jiraAPI;
        this.projectService = projectService;
    }

    public List<Version> findAll() {
        return versionRepository.findAll();
    }

    public Version findById(Long id) {
        return versionRepository.findById(id).orElseThrow(RuntimeException::new);
    }

    public void syncVersions(List<Project> projects) {
        projects.forEach(project -> {
            List<JiraVersionResponseDTO> jiraVersionResponseDTOS = jiraAPI.listVersionsByProject(project.getKey()).getBody();
            assert jiraVersionResponseDTOS != null;
            List<Version> versions = jiraVersionResponseDTOS.stream().map(this::toDomain).toList();
            versions.forEach(version -> version.setProject(project));
            versionRepository.saveAll(versions);
        });
    }

    public Version toDomain(JiraVersionResponseDTO jiraVersionResponseDTO) {
        Version version = new Version();
        BeanUtils.copyProperties(jiraVersionResponseDTO, version);
        version.setId(Long.parseLong(jiraVersionResponseDTO.getId()));
        version.setReleaseDate(nonNull(jiraVersionResponseDTO.getReleaseDate()) ? LocalDate.parse(jiraVersionResponseDTO.getReleaseDate()) : null);
        version.setStartDate(nonNull(jiraVersionResponseDTO.getStartDate()) ? LocalDate.parse(jiraVersionResponseDTO.getStartDate()) : null);
        return version;
    }

    public VersionDTO toDTO(Version version) {
        VersionDTO versionDTO = new VersionDTO();
        BeanUtils.copyProperties(version, versionDTO);
        return versionDTO;
    }

}
