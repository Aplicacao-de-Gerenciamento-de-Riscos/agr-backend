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

    void processIssueDTO(IssueDTO issueDTO) {
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

    void updateIssueIfChanged(Issue existingIssue, IssueDTO newIssueDTO) {
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

        // Verifica se a lista de issues não é nula antes de processar
        if (nonNull(jiraIssueResponseDTO) && nonNull(jiraIssueResponseDTO.getIssues())) {
            jiraIssueResponseDTO.getIssues().forEach(jiraIssueResponseDTO1 -> {
                IssueDTO issueDTO = new IssueDTO();

                // Verificações de nulidade para cada campo
                if (nonNull(jiraIssueResponseDTO1.getId())) {
                    issueDTO.setId(Long.parseLong(jiraIssueResponseDTO1.getId())); // Converte o ID recebido do Jira (String) para Long (ID do banco de dados)
                }
                if (nonNull(jiraIssueResponseDTO1.getKey())) {
                    issueDTO.setKey(jiraIssueResponseDTO1.getKey());
                }
                if (nonNull(jiraIssueResponseDTO1.getFields()) && nonNull(jiraIssueResponseDTO1.getFields().getStatus())) {
                    issueDTO.setStatus(jiraIssueResponseDTO1.getFields().getStatus().getName());
                }
                if (nonNull(jiraIssueResponseDTO1.getFields()) && nonNull(jiraIssueResponseDTO1.getFields().getAssignee()) && nonNull(jiraIssueResponseDTO1.getFields().getAssignee().getEmailAddress())) {
                    issueDTO.setAssignee(jiraIssueResponseDTO1.getFields().getAssignee().getEmailAddress());
                } else {
                    issueDTO.setAssignee("UNASSIGNED");
                }
                if (nonNull(jiraIssueResponseDTO1.getFields()) && nonNull(jiraIssueResponseDTO1.getFields().getPriority())) {
                    issueDTO.setPriority(jiraIssueResponseDTO1.getFields().getPriority().getName());
                }
                if (nonNull(jiraIssueResponseDTO1.getFields()) && nonNull(jiraIssueResponseDTO1.getFields().getIssuetype())) {
                    issueDTO.setIssueType(jiraIssueResponseDTO1.getFields().getIssuetype().getName());
                }
                if (nonNull(jiraIssueResponseDTO1.getFields()) && nonNull(jiraIssueResponseDTO1.getFields().getSummary())) {
                    issueDTO.setSummary(jiraIssueResponseDTO1.getFields().getSummary());
                }
                if (nonNull(jiraIssueResponseDTO1.getFields()) && nonNull(jiraIssueResponseDTO1.getFields().getTimespent())) {
                    issueDTO.setTimespent(jiraIssueResponseDTO1.getFields().getTimespent());
                }
                if (nonNull(jiraIssueResponseDTO1.getFields()) && nonNull(jiraIssueResponseDTO1.getFields().getTimeestimate())) {
                    issueDTO.setTimeEstimate(jiraIssueResponseDTO1.getFields().getTimeestimate());
                }
                if (nonNull(jiraIssueResponseDTO1.getFields()) && nonNull(jiraIssueResponseDTO1.getFields().getTimeoriginalestimate())) {
                    issueDTO.setTimeOriginalEstimate(jiraIssueResponseDTO1.getFields().getTimeoriginalestimate());
                }
                if (nonNull(jiraIssueResponseDTO1.getFields()) && nonNull(jiraIssueResponseDTO1.getFields().getWorkratio())) {
                    issueDTO.setWorkRatio(jiraIssueResponseDTO1.getFields().getWorkratio());
                }
                if (nonNull(jiraIssueResponseDTO1.getFields()) && nonNull(jiraIssueResponseDTO1.getFields().getComponents())) {
                    issueDTO.setComponents(jiraIssueResponseDTO1.getFields().getComponents().stream().map(componentService::toDto).toList());
                }
                if (nonNull(sprint)) {
                    issueDTO.setSprint(sprintService.toDto(sprint));
                }
                if (nonNull(jiraIssueResponseDTO1.getFields()) && nonNull(jiraIssueResponseDTO1.getFields().getEpic())) {
                    issueDTO.setEpic(epicService.toDto(jiraIssueResponseDTO1.getFields().getEpic()));
                }
                if (nonNull(jiraIssueResponseDTO1.getFields()) && nonNull(jiraIssueResponseDTO1.getFields().getResolutiondate())) {
                    issueDTO.setResolutionDate(LocalDateTime.parse(jiraIssueResponseDTO1.getFields().getResolutiondate(), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")));
                }
                if (nonNull(jiraIssueResponseDTO1.getFields()) && nonNull(jiraIssueResponseDTO1.getFields().getUpdated())) {
                    issueDTO.setUpdated(LocalDateTime.parse(jiraIssueResponseDTO1.getFields().getUpdated(), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")));
                }
                if (nonNull(jiraIssueResponseDTO1.getFields()) && nonNull(jiraIssueResponseDTO1.getFields().getCreated())) {
                    issueDTO.setCreated(LocalDateTime.parse(jiraIssueResponseDTO1.getFields().getCreated(), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")));
                }
                if (nonNull(jiraIssueResponseDTO1.getFields()) && jiraIssueResponseDTO1.getFields().isFlagged()) {
                    issueDTO.setFlagged(jiraIssueResponseDTO1.getFields().isFlagged());
                }

                if (nonNull(jiraIssueResponseDTO1.getFields()) && nonNull(jiraIssueResponseDTO1.getFields().getWorklog())) {
                    issueDTO.setWorklog(worklogService.toDTO(jiraIssueResponseDTO1.getFields().getWorklog())); // Converte o worklog
                }

                if (nonNull(jiraIssueResponseDTO1.getFields()) && nonNull(jiraIssueResponseDTO1.getFields().getParent())) {
                    JiraIssueResponseDTO.Parent parentResponse = jiraIssueResponseDTO1.getFields().getParent();
                    IssueDTO parentIssueDTO = new IssueDTO();

                    if (nonNull(parentResponse.getId())) {
                        parentIssueDTO.setId(Long.parseLong(parentResponse.getId()));
                    }
                    if (nonNull(parentResponse.getKey())) {
                        parentIssueDTO.setKey(parentResponse.getKey());
                    }
                    if (nonNull(parentResponse.getFields()) && nonNull(parentResponse.getFields().getSummary())) {
                        parentIssueDTO.setSummary(parentResponse.getFields().getSummary());
                    }
                    if (nonNull(parentResponse.getFields()) && nonNull(parentResponse.getFields().getIssuetype()) && nonNull(parentResponse.getFields().getIssuetype().getName())) {
                        parentIssueDTO.setIssueType(parentResponse.getFields().getIssuetype().getName());
                    }
                    if (nonNull(parentResponse.getFields()) && nonNull(parentResponse.getFields().getPriority()) && nonNull(parentResponse.getFields().getPriority().getName())) {
                        parentIssueDTO.setPriority(parentResponse.getFields().getPriority().getName());
                    }
                    if (nonNull(parentResponse.getFields()) && nonNull(parentResponse.getFields().getStatus()) && nonNull(parentResponse.getFields().getStatus().getName())) {
                        parentIssueDTO.setStatus(parentResponse.getFields().getStatus().getName());
                    }

                    issueDTO.setParent(parentIssueDTO);
                }

                if (nonNull(jiraIssueResponseDTO1.getFields()) && nonNull(jiraIssueResponseDTO1.getFields().getFixVersions())) {
                    List<VersionDTO> versionDTOS = new ArrayList<>();
                    jiraIssueResponseDTO1.getFields().getFixVersions().forEach(version -> {
                        // Para cada versão encontrada, busca no banco de dados e converte para VersionDTO
                        versionDTOS.add(versionService.toDTO(versionService.findByIdOrCreate(version)));
                    });
                    issueDTO.setVersion(versionDTOS);
                }

                issueDTOS.add(issueDTO);
            });
        }

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
