package com.catolicasc.agrbackend.feature.metrics.service;

import com.catolicasc.agrbackend.clients.prediction.dto.PredictVersionDelayDTO;
import com.catolicasc.agrbackend.clients.prediction.service.PredictionAPI;
import com.catolicasc.agrbackend.feature.epic.dto.EpicUsageDTO;
import com.catolicasc.agrbackend.feature.epic.repository.EpicRepository;
import com.catolicasc.agrbackend.feature.issue.dto.BugIssuesRelationDTO;
import com.catolicasc.agrbackend.feature.issue.dto.IssuePlanningDTO;
import com.catolicasc.agrbackend.feature.issue.dto.IssueTypeDonePercentageDTO;
import com.catolicasc.agrbackend.feature.issue.repository.IssueRepository;
import com.catolicasc.agrbackend.feature.metrics.dto.*;
import com.catolicasc.agrbackend.feature.sprint.service.SprintService;
import com.catolicasc.agrbackend.feature.version.domain.Version;
import com.catolicasc.agrbackend.feature.version.dto.VersionDTO;
import com.catolicasc.agrbackend.feature.version.service.VersionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
public class MetricsService {

    //Logger
    private static final Logger log = LoggerFactory.getLogger(MetricsService.class);

    //Repositories
    private final IssueRepository issueRepository;

    //Services
    private final VersionService versionService;
    private final SprintService sprintService;

    //Clients
    private final PredictionAPI predictionAPI;
    private final EpicRepository epicRepository;

    public MetricsService(IssueRepository issueRepository, VersionService versionService, SprintService sprintService, PredictionAPI predictionAPI, EpicRepository epicRepository) {
        this.issueRepository = issueRepository;
        this.versionService = versionService;
        this.sprintService = sprintService;
        this.predictionAPI = predictionAPI;
        this.epicRepository = epicRepository;
    }

    /**
     * Retorna a porcentagem de entrega por tipo de issue
     * @param versionId
     * @param sprintId
     * @return
     */
    public DelieveryPercentagePerIssueTypeDTO getDelieveryPercentagePerIssueType(Long versionId, Long sprintId) {
        if (isNull(versionId) && isNull(sprintId)) {
            log.info("VersionId and SprintId are null");
        }

        List<IssueTypeDonePercentageDTO> issueTypeDonePercentageDTOS = issueRepository.findDonePercentageByIssueType(versionId, sprintId);

        DelieveryPercentagePerIssueTypeDTO delieveryPercentagePerIssueTypeDTO = new DelieveryPercentagePerIssueTypeDTO();
        delieveryPercentagePerIssueTypeDTO.setDelieveryPercentagePerIssueType(issueTypeDonePercentageDTOS);
        if (!isNull(versionId)) {
            delieveryPercentagePerIssueTypeDTO.setVersion(versionService.getVersionDTO(versionId));
        }
        if (!isNull(sprintId)) {
            delieveryPercentagePerIssueTypeDTO.setSprint(sprintService.getSprintDTO(sprintId));
        }

        return delieveryPercentagePerIssueTypeDTO;
    }

    public WorkPlanningVarianceDTO getWorkPlanningVariance(Long versionId, Long sprintId) {
        if (isNull(versionId) && isNull(sprintId)) {
            log.info("VersionId and SprintId are null");
        }

        IssuePlanningDTO workPlanningVarianceDTO = issueRepository.findWorkPlanningVariance(versionId, sprintId);

        WorkPlanningVarianceDTO workPlanningVariance = new WorkPlanningVarianceDTO();
        workPlanningVariance.setIssuePlanning(workPlanningVarianceDTO);
        if (!isNull(versionId)) {
            workPlanningVariance.setVersion(versionService.getVersionDTO(versionId));
        }
        if (!isNull(sprintId)) {
            workPlanningVariance.setSprint(sprintService.getSprintDTO(sprintId));
        }

        return workPlanningVariance;
    }

    /**
     * Retorna a previsão de atraso para cada versão
     * @param versionIds
     * @return
     */
    public List<PredictDelayPerVersionDTO> getDelayPrediction(List<Long> versionIds) {
        String versionIdsString = String.join(",", versionIds.toString().replace("[", "").replace("]", "").split(", "));
        List<PredictVersionDelayDTO> predictVersionDelayDTOS = predictionAPI.predictDelay(versionIdsString);
        List<PredictDelayPerVersionDTO> predictDelayPerVersionDTOS = new ArrayList<>();
        for (PredictVersionDelayDTO predictVersionDelayDTO : predictVersionDelayDTOS) {
            PredictDelayPerVersionDTO predictDelayPerVersionDTO = new PredictDelayPerVersionDTO();
            predictDelayPerVersionDTO.setVersion(versionService.getVersionDTO(predictVersionDelayDTO.getVersionId()));
            predictDelayPerVersionDTO.setDelayRiskPercentage(predictVersionDelayDTO.getDelayRiskPercentage());
            predictDelayPerVersionDTOS.add(predictDelayPerVersionDTO);
        }

        return predictDelayPerVersionDTOS;
    }

    /**
     * Retorna a relação de issues críticas (CRITICAL ou BLOCKER) e não críticas por versão ou sprint
     * @param versionId
     * @param sprintId
     * @return
     */
    public List<CriticalIssueRelationDTO> getCriticalIssuesRelation(List<Long> versionId, List<Long> sprintId) {
        if (isNull(versionId) && isNull(sprintId)) {
            log.info("VersionId and SprintId are null");
        }

        List<CriticalIssueRelationDTO> criticalIssueRelationDTOS = new ArrayList<>();

        if (nonNull(versionId) && !versionId.isEmpty()) {
            List<com.catolicasc.agrbackend.feature.issue.dto.CriticalIssueRelationDTO> criticalIssueRelationDTOPersisted = issueRepository.findCriticalIssuesByVersions(versionId);
            // Consolidar os resultados por ID (version)
            Map<Long, com.catolicasc.agrbackend.feature.issue.dto.CriticalIssueRelationDTO> consolidatedResults =
                    criticalIssueRelationDTOPersisted.stream()
                            .collect(Collectors.groupingBy(
                                    com.catolicasc.agrbackend.feature.issue.dto.CriticalIssueRelationDTO::getId,
                                    Collectors.reducing(
                                            new com.catolicasc.agrbackend.feature.issue.dto.CriticalIssueRelationDTO(0L, 0L, 0L, 0L, 0.0, 0.0),
                                            (dto1, dto2) -> new com.catolicasc.agrbackend.feature.issue.dto.CriticalIssueRelationDTO(
                                                    dto1.getId(),
                                                    dto1.getCriticalIssuesCount() + dto2.getCriticalIssuesCount(),
                                                    dto1.getNonCriticalIssuesCount() + dto2.getNonCriticalIssuesCount(),
                                                    dto1.getTotalIssuesCount() + dto2.getTotalIssuesCount(),
                                                    0.0, // Porcentagens serão recalculadas posteriormente
                                                    0.0 // Porcentagens serão recalculadas posteriormente
                                            )
                                    )
                            ));

            // Recalcular porcentagens e associar ao DTO final
            consolidatedResults.forEach((id, consolidatedDTO) -> {
                long totalIssuesCount = consolidatedDTO.getTotalIssuesCount();
                if (totalIssuesCount > 0) {
                    consolidatedDTO.setCriticalIssuesPercentage(round((consolidatedDTO.getCriticalIssuesCount() * 100.0) / totalIssuesCount, 1));
                    consolidatedDTO.setNonCriticalIssuesPercentage(round((consolidatedDTO.getNonCriticalIssuesCount() * 100.0) / totalIssuesCount, 1));
                }
                CriticalIssueRelationDTO criticalIssueRelationDTO = new CriticalIssueRelationDTO();
                criticalIssueRelationDTO.setVersion(versionService.getVersionDTO(id));
                criticalIssueRelationDTO.setCriticalIssueRelation(List.of(consolidatedDTO));
                criticalIssueRelationDTOS.add(criticalIssueRelationDTO);
            });
        } else {
            List<com.catolicasc.agrbackend.feature.issue.dto.CriticalIssueRelationDTO> criticalIssueRelationDTOPersisted = issueRepository.findCriticalIssuesBySprints(sprintId);
            // Consolidar os resultados por ID (sprint)
            Map<Long, com.catolicasc.agrbackend.feature.issue.dto.CriticalIssueRelationDTO> consolidatedResults =
                    criticalIssueRelationDTOPersisted.stream()
                            .collect(Collectors.groupingBy(
                                    com.catolicasc.agrbackend.feature.issue.dto.CriticalIssueRelationDTO::getId,
                                    Collectors.reducing(
                                            new com.catolicasc.agrbackend.feature.issue.dto.CriticalIssueRelationDTO(0L, 0L, 0L, 0L, 0.0, 0.0),
                                            (dto1, dto2) -> new com.catolicasc.agrbackend.feature.issue.dto.CriticalIssueRelationDTO(
                                                    dto1.getId(),
                                                    dto1.getCriticalIssuesCount() + dto2.getCriticalIssuesCount(),
                                                    dto1.getNonCriticalIssuesCount() + dto2.getNonCriticalIssuesCount(),
                                                    dto1.getTotalIssuesCount() + dto2.getTotalIssuesCount(),
                                                    0.0, // Porcentagens serão recalculadas posteriormente
                                                    0.0 // Porcentagens serão recalculadas posteriormente
                                            )
                                    )
                            ));

            // Recalcular porcentagens e associar ao DTO final
            consolidatedResults.forEach((id, consolidatedDTO) -> {
                long totalIssuesCount = consolidatedDTO.getTotalIssuesCount();
                if (totalIssuesCount > 0) {
                    consolidatedDTO.setCriticalIssuesPercentage(round((consolidatedDTO.getCriticalIssuesCount() * 100.0) / totalIssuesCount, 1));
                    consolidatedDTO.setNonCriticalIssuesPercentage(round((consolidatedDTO.getNonCriticalIssuesCount() * 100.0) / totalIssuesCount, 1));
                }
                CriticalIssueRelationDTO criticalIssueRelationDTO = new CriticalIssueRelationDTO();
                criticalIssueRelationDTO.setSprint(sprintService.getSprintDTO(id));
                criticalIssueRelationDTO.setCriticalIssueRelation(List.of(consolidatedDTO));
                criticalIssueRelationDTOS.add(criticalIssueRelationDTO);
            });
        }

        return criticalIssueRelationDTOS;
    }

    /**
     * Retorna a relação de issues do tipo 'BUG' e não 'BUG' por versão ou sprint
     * @param versionId
     * @param sprintId
     * @return
     */
    public List<BugIssueRelationDTO> getBugIssuesRelation(List<Long> versionId, List<Long> sprintId) {
        if (isNull(versionId) && isNull(sprintId)) {
            log.info("VersionId and SprintId are null");
        }

        List<BugIssueRelationDTO> bugIssueRelationDTOS = new ArrayList<>();
        if (nonNull(versionId) && !versionId.isEmpty()) {
            List<BugIssuesRelationDTO> bugIssuesRelationDTOPersisted = issueRepository.findBugIssuesByVersions(versionId);
            versionId.forEach(id -> {
                BugIssueRelationDTO bugIssueRelationDTO = new BugIssueRelationDTO();
                bugIssueRelationDTO.setVersion(versionService.getVersionDTO(id));
                bugIssueRelationDTO.setBugIssuesRelation(bugIssuesRelationDTOPersisted.stream().filter(bugIssuesRelationDTO -> bugIssuesRelationDTO.getId().equals(id)).toList());
                bugIssueRelationDTOS.add(bugIssueRelationDTO);
            });
        } else {
            List<BugIssuesRelationDTO> bugIssuesRelationDTOPersisted = issueRepository.findBugIssuesBySprints(sprintId);
            sprintId.forEach(id -> {
                BugIssueRelationDTO bugIssueRelationDTO = new BugIssueRelationDTO();
                bugIssueRelationDTO.setSprint(sprintService.getSprintDTO(id));
                bugIssueRelationDTO.setBugIssuesRelation(bugIssuesRelationDTOPersisted.stream().filter(bugIssuesRelationDTO -> bugIssuesRelationDTO.getId().equals(id)).toList());
                bugIssueRelationDTOS.add(bugIssueRelationDTO);
            });
        }

        return bugIssueRelationDTOS;
    }

    // Método utilitário para arredondamento
    private double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException("O número de casas decimais deve ser positivo");
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP); // Arredondamento para o mais próximo
        return bd.doubleValue();
    }

    /**
     * Retorna a relação de uso de épicos por versão
     * @param versionIds
     * @return
     */
    public List<EpicPerVersionDTO> getEpicsPerVersion(List<Long> versionIds) {
        List<EpicPerVersionDTO> epicPerVersionDTOS = new ArrayList<>();
        List<EpicUsageDTO> epicUsageDTOS = epicRepository.findEpicUsagesByVersions(versionIds);

        versionIds.forEach(versionId -> {
            EpicPerVersionDTO epicPerVersionDTO = new EpicPerVersionDTO();
            epicPerVersionDTO.setVersion(versionService.getVersionDTO(versionId));

            // Filtrar apenas os épicos da versão atual e remover aqueles sem tempo gasto
            List<EpicUsageDTO> filteredEpics = new ArrayList<>(epicUsageDTOS.stream()
                    .filter(epicUsageDTO -> epicUsageDTO.getVersionId().equals(versionId))
                    .filter(epicUsageDTO -> epicUsageDTO.getTimespent() != null && epicUsageDTO.getTimespent() > 0)
                    .toList());

            // Ordenar por tempo gasto em ordem decrescente
            filteredEpics.sort((e1, e2) -> e2.getTimespent().compareTo(e1.getTimespent()));

            // Manter apenas os 5 mais usados (ou menos se não houver suficientes)
            List<EpicUsageDTO> top5Epics = filteredEpics.size() > 5
                    ? filteredEpics.subList(0, 5)
                    : filteredEpics;

            epicPerVersionDTO.setEpicsUsage(new ArrayList<>(top5Epics)); // Garantir lista mutável, se necessário
            epicPerVersionDTOS.add(epicPerVersionDTO);
        });

        return epicPerVersionDTOS;
    }
}
