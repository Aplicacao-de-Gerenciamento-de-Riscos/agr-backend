package com.catolicasc.agrbackend.feature.sprint.service;

import com.catolicasc.agrbackend.clients.jira.dto.JiraIssueResponseDTO;
import com.catolicasc.agrbackend.clients.jira.dto.JiraSprintResponseDTO;
import com.catolicasc.agrbackend.clients.jira.service.JiraAPI;
import com.catolicasc.agrbackend.feature.sprint.domain.Sprint;
import com.catolicasc.agrbackend.feature.sprint.dto.SprintDTO;
import com.catolicasc.agrbackend.feature.sprint.repository.SprintRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static java.util.Objects.nonNull;

@Service
public class SprintService {

    private final SprintRepository sprintRepository;
    private final JiraAPI jiraAPI;

    public SprintService(
        SprintRepository sprintRepository,
        JiraAPI jiraAPI) {
        this.sprintRepository = sprintRepository;
        this.jiraAPI = jiraAPI;
    }

    public Sprint findById(Long id) {
        return sprintRepository.findById(id).orElse(null);
    }

    public List<Sprint> findAll() {
        return sprintRepository.findAll();
    }

    public Sprint toDomain(JiraIssueResponseDTO.Sprint sprint) {
        Sprint sprintDomain = new Sprint();
        sprintDomain.setId(Long.parseLong(sprint.getId()));
        sprintDomain.setName(sprint.getName());
        return sprintDomain;
    }

    public Sprint toDomain(SprintDTO sprintDTO) {
        Sprint sprint = new Sprint();
        BeanUtils.copyProperties(sprintDTO, sprint);
        return sprint;
    }

    public SprintDTO toDto(JiraSprintResponseDTO.SprintResponse sprint) {
        SprintDTO sprintDTO = new SprintDTO();
        BeanUtils.copyProperties(sprint, sprintDTO);
        sprintDTO.setId(Long.parseLong(sprint.getId()));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");

        sprintDTO.setStartDate(nonNull(sprint.getStartDate()) ? LocalDateTime.parse(sprint.getStartDate(), formatter).atOffset(ZoneOffset.UTC).toLocalDateTime() : null);
        sprintDTO.setEndDate(nonNull(sprint.getEndDate()) ? LocalDateTime.parse(sprint.getEndDate(), formatter).atOffset(ZoneOffset.UTC).toLocalDateTime() : null);
        sprintDTO.setCompleteDate(nonNull(sprint.getCompleteDate()) ? LocalDateTime.parse(sprint.getCompleteDate(), formatter).atOffset(ZoneOffset.UTC).toLocalDateTime() : null);

        return sprintDTO;
    }

    public SprintDTO toDto(Sprint sprint) {
        SprintDTO sprintDTO = new SprintDTO();
        BeanUtils.copyProperties(sprint, sprintDTO);
        return sprintDTO;
    }

    public CompletableFuture<List<SprintDTO>> syncSprintsByBoardAsync(String boardId) {
        return CompletableFuture.supplyAsync(() -> {
            List<SprintDTO> sprintDTOS = new ArrayList<>();
            Long startAt = 0L;
            boolean hasMoreSprints;

            do {
                // Busca de forma síncrona a resposta da API do Jira
                JiraSprintResponseDTO jiraSprintResponseDTO = jiraAPI.listSprintsByBoard(boardId, startAt).getBody();
                assert jiraSprintResponseDTO != null;

                if (!jiraSprintResponseDTO.getValues().isEmpty()) {
                    List<JiraSprintResponseDTO.SprintResponse> sprintResponses = jiraSprintResponseDTO.getValues();

                    List<CompletableFuture<Void>> futures = new ArrayList<>();

                    for (JiraSprintResponseDTO.SprintResponse sprintResponse : sprintResponses) {
                        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                            SprintDTO sprintDTO = toDto(sprintResponse);

                            Sprint sprint = sprintRepository.findById(Long.parseLong(sprintResponse.getId())).orElse(null);

                            if (nonNull(sprint)) {
                                updateSprintIfChanged(sprint, sprintDTO);
                            } else {
                                sprint = toDomain(sprintDTO);
                            }

                            sprintRepository.save(sprint);  // Salva no banco de forma assíncrona
                            sprintDTOS.add(sprintDTO);      // Adiciona ao retorno
                        });
                        futures.add(future);
                    }

                    // Espera que todas as operações assíncronas terminem antes de continuar
                    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
                }

                hasMoreSprints = !jiraSprintResponseDTO.isLast();
                startAt++;

            } while (hasMoreSprints);

            return sprintDTOS;
        });
    }

    public List<SprintDTO> syncSprintsByBoard(String boardId) {
        List<SprintDTO> sprintDTOS = new ArrayList<>();
        Long startAt = 0L;
        boolean hasMoreSprints;

        do {
            JiraSprintResponseDTO jiraSprintResponseDTO = jiraAPI.listSprintsByBoard(boardId, startAt).getBody();
            assert jiraSprintResponseDTO != null;

            if (!jiraSprintResponseDTO.getValues().isEmpty()) {
                List<JiraSprintResponseDTO.SprintResponse> sprintResponses = jiraSprintResponseDTO.getValues();

                for (JiraSprintResponseDTO.SprintResponse sprintResponse : sprintResponses) {
                    SprintDTO sprintDTO = toDto(sprintResponse);

                    Sprint sprint = sprintRepository.findById(Long.parseLong(sprintResponse.getId())).orElse(null);

                    if (nonNull(sprint)) {
                        updateSprintIfChanged(sprint, sprintDTO);
                    } else {
                        sprint = toDomain(sprintDTO);
                    }

                    sprintRepository.save(sprint);  // Salva imediatamente no banco
                    sprintDTOS.add(sprintDTO);      // Adiciona ao retorno
                }
            }

            hasMoreSprints = !jiraSprintResponseDTO.isLast();
            startAt++;

        } while (hasMoreSprints);

        return sprintDTOS;
    }



    private void updateSprintIfChanged(Sprint sprint, SprintDTO sprintDTO) {
        updatePropertyIfChanged(sprint::getName, sprint::setName, sprintDTO.getName());
        updatePropertyIfChanged(sprint::getState, sprint::setState, sprintDTO.getState());
        updatePropertyIfChanged(sprint::getStartDate, sprint::setStartDate, sprintDTO.getStartDate());
        updatePropertyIfChanged(sprint::getEndDate, sprint::setEndDate, sprintDTO.getEndDate());
        updatePropertyIfChanged(sprint::getCompleteDate, sprint::setCompleteDate, sprintDTO.getCompleteDate());
        updatePropertyIfChanged(sprint::getGoal, sprint::setGoal, sprintDTO.getGoal());
    }

    private <T> void updatePropertyIfChanged(Supplier<T> getter, Consumer<T> setter, T newValue) {
        if (nonNull(newValue) && !Objects.equals(getter.get(), newValue)) {
            setter.accept(newValue);
        }
    }

    public SprintDTO getSprintDTO(Long sprintId) {
        return toDto(findById(sprintId));
    }

}
