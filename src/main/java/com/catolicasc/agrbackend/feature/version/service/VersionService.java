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
import java.util.ArrayList;
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

    public VersionService(VersionRepository versionRepository, JiraAPI jiraAPI) {
        this.versionRepository = versionRepository;
        this.jiraAPI = jiraAPI;
    }

    /**
     * Busca todas as versões de um projeto
     * @param projectId Identificador do projeto
     * @return Lista de versões
     */
    public List<VersionDTO> findAllByProjectId(List<Long> projectId) {
        List<VersionDTO> versionDTOS = new ArrayList<>();
        projectId.forEach(id -> {
            List<Version> versions = versionRepository.findAllByProjectId(id);
            versionDTOS.addAll(versions.stream().map(this::toDTO).toList());
        });
        return versionDTOS;
    }

    /**
     * Busca um versão no banco de dados
     *
     * @param id Identificador da versão
     * @return Versão
     */
    public Version findById(Long id) {
        return versionRepository.findById(id).orElseThrow(RuntimeException::new);
    }

    /**
     * Sincroniza as versões de um projeto com o Jira
     *
     * @param projects Lista de projetos
     */
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

    /**
     * Converte um VersionDTO para um Version
     *
     * @param versionDTO Objeto retornado pela API do Jira
     * @return Version convertido
     */
    public Version toDomain(VersionDTO versionDTO) {
        Version version = new Version();
        BeanUtils.copyProperties(versionDTO, version);
        return version;
    }

    /**
     * Converte um Version para um VersionDTO
     *
     * @param version Objeto retornado pela API do Jira
     * @return VersionDTO convertido
     */
    public VersionDTO toDTO(Version version) {
        VersionDTO versionDTO = new VersionDTO();
        BeanUtils.copyProperties(version, versionDTO);
        return versionDTO;
    }

    /**
     * Converte um JiraVersionResponseDTO para um VersionDTO
     *
     * @param jiraVersionResponseDTO Objeto retornado pela API do Jira
     * @return VersionDTO convertido
     */
    private VersionDTO toDTO(JiraVersionResponseDTO jiraVersionResponseDTO) {
        VersionDTO versionDTO = new VersionDTO();
        BeanUtils.copyProperties(jiraVersionResponseDTO, versionDTO);
        versionDTO.setId(Long.parseLong(jiraVersionResponseDTO.getId()));
        versionDTO.setReleaseDate(nonNull(jiraVersionResponseDTO.getReleaseDate()) ? LocalDate.parse(jiraVersionResponseDTO.getReleaseDate()) : null);
        versionDTO.setStartDate(nonNull(jiraVersionResponseDTO.getStartDate()) ? LocalDate.parse(jiraVersionResponseDTO.getStartDate()) : null);
        return versionDTO;
    }

    /**
     * Atualiza os atributos de uma versão caso tenham sido alterados
     *
     * @param version    Versão a ser atualizada
     * @param versionDTO Versão com os novos valores
     */
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

    /**
     * Atualiza uma propriedade caso o valor seja diferente
     *
     * @param getter   Método para obter o valor atual
     * @param setter   Método para definir o novo valor
     * @param newValue Novo valor
     * @param <T>      Tipo do valor
     */
    private <T> void updatePropertyIfChanged(Supplier<T> getter, Consumer<T> setter, T newValue) {
        if (nonNull(newValue) && !Objects.equals(getter.get(), newValue)) {
            setter.accept(newValue);
        }
    }

    /**
     * Busca uma versão no banco de dados, caso não exista, cria uma nova
     *
     * @param versionId
     * @return
     */
    public VersionDTO getVersionDTO(Long versionId) {
        return toDTO(findById(versionId));
    }

}
