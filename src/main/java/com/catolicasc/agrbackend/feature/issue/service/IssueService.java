package com.catolicasc.agrbackend.feature.issue.service;

import com.catolicasc.agrbackend.clients.jira.dto.JiraIssueResponseDTO;
import com.catolicasc.agrbackend.clients.jira.service.JiraAPI;
import com.catolicasc.agrbackend.feature.component.service.ComponentService;
import com.catolicasc.agrbackend.feature.epic.service.EpicService;
import com.catolicasc.agrbackend.feature.issue.domain.Issue;
import com.catolicasc.agrbackend.feature.issue.dto.IssueDTO;
import com.catolicasc.agrbackend.feature.issue.repository.IssueRepository;
import com.catolicasc.agrbackend.feature.sprint.domain.Sprint;
import com.catolicasc.agrbackend.feature.sprint.service.SprintService;
import com.catolicasc.agrbackend.feature.version.dto.VersionDTO;
import com.catolicasc.agrbackend.feature.version.service.VersionService;
import com.catolicasc.agrbackend.feature.versionissue.domain.VersionIssue;
import com.catolicasc.agrbackend.feature.versionissue.service.VersionIssueService;
import com.catolicasc.agrbackend.feature.worklog.domain.Worklog;
import com.catolicasc.agrbackend.feature.worklog.service.WorklogService;
import com.catolicasc.agrbackend.feature.worklogentry.service.WorkLogEntryService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Transactional
@Service
public class IssueService {

    private static final Logger log = LoggerFactory.getLogger(IssueService.class);
    private final IssueRepository issueRepository;
    private final JiraAPI jiraAPI;
    private final ComponentService componentService;
    private final SprintService sprintService;
    private final EpicService epicService;
    private final WorklogService worklogService;
    private final VersionService versionService;
    private final WorkLogEntryService workLogEntryService;

    public IssueService(
            IssueRepository issueRepository,
            JiraAPI jiraAPI,
            ComponentService componentService,
            SprintService sprintService,
            EpicService epicService,
            WorklogService worklogService,
            VersionService versionService,
            VersionIssueService versionIssueService, WorkLogEntryService workLogEntryService) {
        this.issueRepository = issueRepository;
        this.jiraAPI = jiraAPI;
        this.componentService = componentService;
        this.sprintService = sprintService;
        this.epicService = epicService;
        this.worklogService = worklogService;
        this.versionService = versionService;
        this.workLogEntryService = workLogEntryService;
    }

    public JiraIssueResponseDTO listIssuesBySprint(String sprintId) {
        try {
            return jiraAPI.listIssuesBySprint(sprintId, 1000L).getBody();
        } catch (Exception e) {
            log.error("Erro ao buscar issues do sprint {}", sprintId, e);
            return new JiraIssueResponseDTO();
        }
    }

    /**
     * Sincroniza todas as Issues de todos os Sprints
     */
    public void syncIssuesBySprints() {
        List<Sprint> sprints = sprintService.findAll();
        sprints.forEach(this::syncIssuesBySprint);
    }

    /**
     * Sincroniza todas as Issues de um Sprint específico
     *
     * @param sprint Sprint a ser sincronizado
     */
    public void syncIssuesBySprint(Sprint sprint) {
        JiraIssueResponseDTO jiraIssueResponseDTO = listIssuesBySprint(sprint.getId().toString());
        if (isNull(jiraIssueResponseDTO.getIssues())) {
            log.error("Erro ao buscar issues do sprint {}", sprint.getId());
            return;
        }
        List<IssueDTO> issueDTOS = getIssueDTO(jiraIssueResponseDTO, sprint); // Converte a resposta da API do Jira em uma lista de IssueDTO

        // Processamento sequencial das IssueDTOs
        issueDTOS.forEach(this::processIssueDTO);
    }

    private void processIssueDTO(IssueDTO issueDTO) {
        // Evita processar a mesma Issue mais de uma vez
        // Cria uma lista de IDs processados
        Set<Long> processedIds = new HashSet<>();

        // Processa a IssueDTO recursivamente
        processIssueDTORecursive(issueDTO, processedIds);
    }

    private void processIssueDTORecursive(IssueDTO issueDTO, Set<Long> processedIds) {
        if (processedIds.contains(issueDTO.getId())) {
            return;
        }
        processedIds.add(issueDTO.getId());

        // Processar a parent Issue primeiro
        if (issueDTO.getParent() != null) {
            processIssueDTORecursive(issueDTO.getParent(), processedIds);
        }

        Issue existingIssue = issueRepository.findById(issueDTO.getId()).orElse(null);

        // Verifica se a issue já existe no banco de dados
        if (nonNull(existingIssue)) {
            // Atualiza a issue existente
            updateIssueIfChanged(existingIssue, issueDTO);
            // Atualiza a lista de versões da issue
            updateIssueVersionIssues(existingIssue, issueDTO);
            // Salva a issue atualizada
            issueRepository.save(existingIssue);
            // Processa o worklog da issue
            proccessIssueWorklog(existingIssue, issueDTO);
        } else {
            Issue newIssue = toDomain(issueDTO, processedIds);
            issueRepository.save(newIssue);
            proccessIssueWorklog(newIssue, issueDTO);
        }
    }

    private void proccessIssueWorklog(Issue issue, IssueDTO issueDTO) {
        if (nonNull(issueDTO.getWorklog())) {
            Worklog worklog = issue.getWorklog();

            if (worklog == null) {
                // Cria e salva um novo Worklog
                worklog = worklogService.toDomain(issueDTO.getWorklog());
                worklog = worklogService.save(worklog);
                issue.setWorklog(worklog);
            } else {
                // Garante que o Worklog existente esteja salvo
                if (worklog.getId() == null) {
                    worklog = worklogService.save(worklog);
                    issue.setWorklog(worklog);
                }
            }

            // Sincroniza as entradas de worklog
            workLogEntryService.syncWorklogEntries(worklog, issueDTO.getWorklog());
        } else {
            log.info("Issue {} não possui worklog", issue.getKey());
        }
    }

    private void updateIssueVersionIssues(Issue issue, IssueDTO issueDTO) {
        // Inicializa a coleção se for nula
        if (issue.getVersionIssues() == null) {
            issue.setVersionIssues(new ArrayList<>());
        }

        // Limpa o conteúdo da lista de versões existentes sem alterar sua referência
        issue.getVersionIssues().clear();

        // Adiciona os novos VersionIssues
        if (nonNull(issueDTO.getVersion()) && !issueDTO.getVersion().isEmpty()) {
            for (VersionDTO versionDTO : issueDTO.getVersion()) {
                VersionIssue versionIssue = VersionIssue.builder()
                        .issue(issue)
                        .version(versionService.findById(versionDTO.getId()))
                        .build();
                issue.getVersionIssues().add(versionIssue);
            }
        }
    }

    private void updateIssueIfChanged(Issue existingIssue, IssueDTO newIssueDTO) {
        updatePropertyIfChanged(existingIssue::getKey, existingIssue::setKey, newIssueDTO.getKey());
        updatePropertyIfChanged(existingIssue::getStatus, existingIssue::setStatus, newIssueDTO.getStatus());
        updatePropertyIfChanged(existingIssue::getAssignee, existingIssue::setAssignee, newIssueDTO.getAssignee());
        updatePropertyIfChanged(existingIssue::getPriority, existingIssue::setPriority, newIssueDTO.getPriority());
        updatePropertyIfChanged(existingIssue::getSummary, existingIssue::setSummary, newIssueDTO.getSummary());
        updatePropertyIfChanged(existingIssue::getTimespent, existingIssue::setTimespent, newIssueDTO.getTimespent());
        updatePropertyIfChanged(existingIssue::getTimeEstimate, existingIssue::setTimeEstimate, newIssueDTO.getTimeEstimate());
        updatePropertyIfChanged(existingIssue::getTimeOriginalEstimate, existingIssue::setTimeOriginalEstimate, newIssueDTO.getTimeOriginalEstimate());
        updatePropertyIfChanged(existingIssue::getWorkRatio, existingIssue::setWorkRatio, newIssueDTO.getWorkRatio());
        updatePropertyIfChanged(existingIssue::getResolutionDate, existingIssue::setResolutionDate, newIssueDTO.getResolutionDate());
        updatePropertyIfChanged(existingIssue::getUpdated, existingIssue::setUpdated, newIssueDTO.getUpdated());
        updatePropertyIfChanged(existingIssue::getCreated, existingIssue::setCreated, newIssueDTO.getCreated());
        updatePropertyIfChanged(existingIssue::getFlagged, existingIssue::setFlagged, newIssueDTO.getFlagged());
        updatePropertyIfChanged(existingIssue::getIssueType, existingIssue::setIssueType, newIssueDTO.getIssueType());
    }

    private <T> void updatePropertyIfChanged(Supplier<T> getter, Consumer<T> setter, T newValue) {
        if (nonNull(newValue) && !Objects.equals(getter.get(), newValue)) {
            setter.accept(newValue);
        }
    }

    /**
     * Converte uma lista de JiraIssueResponseDTO em uma lista de IssueDTO
     *
     * @param jiraIssueResponseDTO Resposta da API do Jira
     * @param sprint                Sprint associada
     * @return Lista de IssueDTO
     */
    public List<IssueDTO> getIssueDTO(JiraIssueResponseDTO jiraIssueResponseDTO, Sprint sprint) {
        List<IssueDTO> issueDTOS = new ArrayList<>();

        jiraIssueResponseDTO.getIssues().forEach(jiraIssueResponseDTO1 -> {
            IssueDTO issueDTO = new IssueDTO();
            issueDTO.setId(Long.parseLong(jiraIssueResponseDTO1.getId())); // Converte o ID recebido do Jira (String) para Long (ID do banco de dados)
            issueDTO.setKey(jiraIssueResponseDTO1.getKey());
            issueDTO.setStatus(jiraIssueResponseDTO1.getFields().getStatus().getName());
            issueDTO.setAssignee(nonNull(jiraIssueResponseDTO1.getFields().getAssignee()) && nonNull(jiraIssueResponseDTO1.getFields().getAssignee().getEmailAddress()) ? jiraIssueResponseDTO1.getFields().getAssignee().getEmailAddress() : "UNASSIGNED");
            issueDTO.setPriority(jiraIssueResponseDTO1.getFields().getPriority().getName());
            issueDTO.setIssueType(jiraIssueResponseDTO1.getFields().getIssuetype().getName());
            issueDTO.setSummary(jiraIssueResponseDTO1.getFields().getSummary());
            issueDTO.setTimespent(jiraIssueResponseDTO1.getFields().getTimespent());
            issueDTO.setTimeEstimate(jiraIssueResponseDTO1.getFields().getTimeestimate());
            issueDTO.setTimeOriginalEstimate(jiraIssueResponseDTO1.getFields().getTimeoriginalestimate());
            issueDTO.setWorkRatio(jiraIssueResponseDTO1.getFields().getWorkratio());
            issueDTO.setComponents(jiraIssueResponseDTO1.getFields().getComponents().stream().map(componentService::toDto).toList());
            issueDTO.setSprint(sprintService.toDto(sprint));
            issueDTO.setEpic(nonNull(jiraIssueResponseDTO1.getFields().getEpic()) ? epicService.toDto(jiraIssueResponseDTO1.getFields().getEpic()) : null);
            issueDTO.setResolutionDate(nonNull(jiraIssueResponseDTO1.getFields().getResolutiondate()) ? LocalDateTime.parse(jiraIssueResponseDTO1.getFields().getResolutiondate(), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")) : null);
            issueDTO.setUpdated(nonNull(jiraIssueResponseDTO1.getFields().getUpdated()) ? LocalDateTime.parse(jiraIssueResponseDTO1.getFields().getUpdated(), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")) : null);
            issueDTO.setCreated(nonNull(jiraIssueResponseDTO1.getFields().getCreated()) ? LocalDateTime.parse(jiraIssueResponseDTO1.getFields().getCreated(), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")) : null);
            issueDTO.setFlagged(jiraIssueResponseDTO1.getFields().isFlagged());

            issueDTO.setWorklog(nonNull(jiraIssueResponseDTO1.getFields().getWorklog()) ? worklogService.toDTO(jiraIssueResponseDTO1.getFields().getWorklog()) : null); // Converte o worklog recebido do Jira e seus WorklogEntries para um WorklogDTO

            if (nonNull(jiraIssueResponseDTO1.getFields().getParent())) {
                // Cria um IssueDTO para o parent
                IssueDTO parentIssueDTO = new IssueDTO();
                JiraIssueResponseDTO.Parent parentResponse = jiraIssueResponseDTO1.getFields().getParent();

                // Preenche o parentIssueDTO com os dados disponíveis
                parentIssueDTO.setId(Long.parseLong(parentResponse.getId()));
                parentIssueDTO.setKey(parentResponse.getKey());
                parentIssueDTO.setSummary(nonNull(parentResponse.getFields().getSummary()) ? parentResponse.getFields().getSummary() : null);
                parentIssueDTO.setIssueType(nonNull(parentResponse.getFields().getIssuetype().getName()) ? parentResponse.getFields().getIssuetype().getName() : null);
                parentIssueDTO.setPriority(nonNull(parentResponse.getFields().getPriority().getName()) ? parentResponse.getFields().getPriority().getName() : null);
                parentIssueDTO.setStatus(nonNull(parentResponse.getFields().getStatus().getName()) ? parentResponse.getFields().getStatus().getName() : null);

                // Define o parent no issueDTO atual
                issueDTO.setParent(parentIssueDTO);
            }

            if (nonNull(jiraIssueResponseDTO1.getFields().getFixVersions())) {
                List<VersionDTO> versionDTOS = new ArrayList<>();

                // Converte as versões do Jira para VersionDTO
                jiraIssueResponseDTO1.getFields().getFixVersions().forEach(version -> {
                    // Para cada versão encontrada, busca no banco de dados e converte para VersionDTO
                    versionDTOS.add(versionService.toDTO(versionService.findByIdOrCreate(version)));
                });
                // Define as versões no issueDTO
                issueDTO.setVersion(versionDTOS);
            }

            issueDTOS.add(issueDTO);
        });

        return issueDTOS;
    }

    /**
     * Converte um IssueDTO para um Issue
     *
     * @param issueDTO      IssueDTO a ser convertido
     * @param processedIds  IDs de Issues já processadas
     * @return Issue convertido
     */
    public Issue toDomain(IssueDTO issueDTO, Set<Long> processedIds) {
        Issue issue = new Issue();
        BeanUtils.copyProperties(issueDTO, issue);

        issue.setComponents(
                nonNull(issueDTO.getComponents())
                        ? issueDTO.getComponents().stream()
                        .map(componentService::findOrCreateComponent)
                        .collect(Collectors.toList())
                        : null
        );

        issue.setEpic(nonNull(issueDTO.getEpic())
                ? epicService.findOrCreateEpic(issueDTO.getEpic())
                : null);

        issue.setSprint(nonNull(issueDTO.getSprint())
                ? sprintService.toDomain(issueDTO.getSprint())
                : null);

        issue.setWorklog(nonNull(issueDTO.getWorklog())
                ? worklogService.toDomain(issueDTO.getWorklog())
                : null);

        // Não definir versionIssues aqui para evitar duplicatas
        issue.setVersionIssues(null);

        // Obter a Issue pai do banco de dados
        if (issueDTO.getParent() != null) {
            Issue parentIssue = issueRepository.findById(issueDTO.getParent().getId()).orElse(null);
            issue.setParent(parentIssue);
        }

        return issue;
    }
}
