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
    private final VersionIssueService versionIssueService;
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
        this.versionIssueService = versionIssueService;
        this.workLogEntryService = workLogEntryService;
    }

    public JiraIssueResponseDTO listIssuesBySprint(String sprintId) {
        return jiraAPI.listIssuesBySprint(sprintId, 1000L).getBody();
    }

    // Removemos o método assíncrono syncIssuesBySprintsAsync()

    public void syncIssuesBySprints() {
        List<Sprint> sprints = sprintService.findAll();
        sprints.forEach(this::syncIssuesBySprint);
    }

    public void syncIssuesBySprint(Sprint sprint) {
        JiraIssueResponseDTO jiraIssueResponseDTO = listIssuesBySprint(sprint.getId().toString());
        List<IssueDTO> issueDTOS = getIssueDTO(jiraIssueResponseDTO, sprint);

        // Processamento sequencial das IssueDTOs
        issueDTOS.forEach(this::processIssueDTO);
    }

    private void processIssueDTO(IssueDTO issueDTO) {
        Set<Long> processedIds = new HashSet<>();
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

        if (nonNull(existingIssue)) {
            updateIssueIfChanged(existingIssue, issueDTO);
            updateIssueVersionIssues(existingIssue, issueDTO);
            issueRepository.save(existingIssue);
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

        // Limpa o conteúdo da coleção existente sem alterar sua referência
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

    public List<IssueDTO> getIssueDTO(JiraIssueResponseDTO jiraIssueResponseDTO, Sprint sprint) {
        List<IssueDTO> issueDTOS = new ArrayList<>();

        jiraIssueResponseDTO.getIssues().forEach(jiraIssueResponseDTO1 -> {
            IssueDTO issueDTO = new IssueDTO();
            issueDTO.setId(Long.parseLong(jiraIssueResponseDTO1.getId()));
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

            issueDTO.setWorklog(nonNull(jiraIssueResponseDTO1.getFields().getWorklog()) ? worklogService.toDTO(jiraIssueResponseDTO1.getFields().getWorklog()) : null);

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
                jiraIssueResponseDTO1.getFields().getFixVersions().forEach(version -> {
                    versionDTOS.add(versionService.toDTO(versionService.findById(Long.parseLong(version.getId()))));
                });
                issueDTO.setVersion(versionDTOS);
            }

            issueDTOS.add(issueDTO);
        });

        return issueDTOS;
    }

    public Issue findIssueById(Long id) {
        return issueRepository.findById(id).orElse(null);
    }

    public Issue getIssueByParent(JiraIssueResponseDTO.Parent parent) {
        return Issue.builder()
                .id(Long.parseLong(parent.getId()))
                .key(parent.getKey())
                .issueType(parent.getFields().getIssuetype().getName())
                .priority(parent.getFields().getPriority().getName())
                .summary(parent.getFields().getSummary())
                .status(parent.getFields().getStatus().getName()).build();
    }

    public IssueDTO toDto(Issue issue) {
        IssueDTO issueDTO = new IssueDTO();
        BeanUtils.copyProperties(issue, issueDTO);
        return issueDTO;
    }

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
