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
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

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

    public CompletableFuture<Void> syncVersionsAsync(List<Project> projects) {
        return CompletableFuture.allOf(
                projects.stream()
                        .map(project -> CompletableFuture.runAsync(() -> {
                            List<JiraVersionResponseDTO> jiraVersionResponseDTOS = jiraAPI.listVersionsByProject(project.getKey()).getBody();
                            assert jiraVersionResponseDTOS != null;

                            List<CompletableFuture<Void>> futures = jiraVersionResponseDTOS.stream()
                                    .map(versionResponse -> CompletableFuture.runAsync(() -> {
                                        VersionDTO versionDTO = toDTO(versionResponse);
                                        Version version = versionRepository.findById(versionDTO.getId()).orElse(null);

                                        if (nonNull(version)) {
                                            updateVersionIfChanged(version, versionDTO);
                                        } else {
                                            version = toDomain(versionDTO);
                                            version.setProject(project);
                                        }

                                        versionRepository.save(version); // Salva no banco de forma assíncrona
                                    }))
                                    .toList();

                            // Espera que todas as operações assíncronas para as versões terminem
                            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
                        }))
                        .toArray(CompletableFuture[]::new)
        );
    }

    public void syncVersions(List<Project> projects) {
        projects.forEach(project -> {
            List<JiraVersionResponseDTO> jiraVersionResponseDTOS = jiraAPI.listVersionsByProject(project.getKey()).getBody();
            assert jiraVersionResponseDTOS != null;
            List<VersionDTO> versions = jiraVersionResponseDTOS.stream().map(this::toDTO).toList();
            versions.forEach(versionDTO -> {
                Version version = versionRepository.findById(versionDTO.getId()).orElse(null);
                if (nonNull(version)) {
                    updateVersionIfChanged(version, versionDTO);
                } else {
                    version = toDomain(versionDTO);
                    version.setProject(project);
                }
                versionRepository.save(version);
            });
        });
    }

    public Version toDomain(VersionDTO versionDTO) {
        Version version = new Version();
        BeanUtils.copyProperties(versionDTO, version);
        return version;
    }

    public VersionDTO toDTO(Version version) {
        VersionDTO versionDTO = new VersionDTO();
        BeanUtils.copyProperties(version, versionDTO);
        return versionDTO;
    }

    private VersionDTO toDTO(JiraVersionResponseDTO jiraVersionResponseDTO) {
        VersionDTO versionDTO = new VersionDTO();
        BeanUtils.copyProperties(jiraVersionResponseDTO, versionDTO);
        versionDTO.setId(Long.parseLong(jiraVersionResponseDTO.getId()));
        versionDTO.setReleaseDate(nonNull(jiraVersionResponseDTO.getReleaseDate()) ? LocalDate.parse(jiraVersionResponseDTO.getReleaseDate()) : null);
        versionDTO.setStartDate(nonNull(jiraVersionResponseDTO.getStartDate()) ? LocalDate.parse(jiraVersionResponseDTO.getStartDate()) : null);
        return versionDTO;
    }

    private void updateVersionIfChanged(Version version, VersionDTO versionDTO) {
        updatePropertyIfChanged(version::getDescription, version::setDescription, versionDTO.getDescription());
        updatePropertyIfChanged(version::getName, version::setName, versionDTO.getName());
        updatePropertyIfChanged(version::getArchived, version::setArchived, versionDTO.getArchived());
        updatePropertyIfChanged(version::getReleased, version::setReleased, versionDTO.getReleased());
        updatePropertyIfChanged(version::getStartDate, version::setStartDate, versionDTO.getStartDate());
        updatePropertyIfChanged(version::getReleaseDate, version::setReleaseDate, versionDTO.getReleaseDate());
        updatePropertyIfChanged(version::getOverdue, version::setOverdue, versionDTO.getOverdue());
        updatePropertyIfChanged(version::getUserStartDate, version::setUserStartDate, versionDTO.getUserStartDate());
        updatePropertyIfChanged(version::getUserReleaseDate, version::setUserReleaseDate, versionDTO.getUserReleaseDate());
    }

    private <T> void updatePropertyIfChanged(Supplier<T> getter, Consumer<T> setter, T newValue) {
        if (nonNull(newValue) && !Objects.equals(getter.get(), newValue)) {
            setter.accept(newValue);
        }
    }

    public VersionDTO getVersionDTO(Long versionId) {
        return toDTO(findById(versionId));
    }

}
