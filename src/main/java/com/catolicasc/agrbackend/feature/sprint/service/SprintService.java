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

    /**
     * Busca um sprint no banco de dados
     *
     * @param id Identificador do sprint
     * @return Sprint
     */
    public Sprint findById(Long id) {
        return sprintRepository.findById(id).orElse(null);
    }

    /**
     * Busca todos os sprints cadastrados no banco de dados
     *
     * @return Lista de sprints
     */
    public List<Sprint> findAll() {
        return sprintRepository.findAll();
    }

    /**
     * Busca todos os sprints cadastrados no banco de dados por projeto
     *
     * @param projectKey Identificador do projeto
     * @return Lista de sprints
     */
    public List<Sprint> findAllByProjectKey(String projectKey) {
        return sprintRepository.findAllByNameContainsIgnoreCase(projectKey);
    }

    /**
     * Converte um SprintDTO para um Sprint
     * @param sprintDTO Objeto retornado pela API do Jira
     * @return Sprint convertido
     */
    public Sprint toDomain(SprintDTO sprintDTO) {
        Sprint sprint = new Sprint();
        BeanUtils.copyProperties(sprintDTO, sprint);
        return sprint;
    }

    /**
     * Converte um SprintResponse para um SprintDTO
     * @param sprint Objeto retornado pela API do Jira
     * @return SprintDTO convertido
     */
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

    /**
     * Converte um Sprint para um SprintDTO
     * @param sprint Objeto retornado pela API do Jira
     * @return SprintDTO convertido
     */
    public SprintDTO toDto(Sprint sprint) {
        SprintDTO sprintDTO = new SprintDTO();
        BeanUtils.copyProperties(sprint, sprintDTO);
        return sprintDTO;
    }

    /**
     * Sincroniza os sprints de um board com o Jira
     *
     * @param boardId Identificador do board
     * @return Lista de sprints sincronizados
     */
    public void syncSprintsByBoard(String boardId) {
        Long startAt = 0L;
        boolean hasMoreSprints;

        // Loop utilizado por conta da paginação do endpoint do Jira
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

                    sprintRepository.save(sprint);     // Adiciona ao retorno
                }
            }

            // Verifica se há mais sprints para buscar
            hasMoreSprints = !jiraSprintResponseDTO.isLast();
            // Atualiza o contador de sprints para a próxima requisição estar na próxima página
            startAt++;

        } while (hasMoreSprints); // Enquanto houver mais sprints para buscar
    }

    /**
     * Atualiza as propriedades do sprint se houver alterações
     * @param sprint Sprint a ser atualizado
     * @param sprintDTO Sprint com os novos valores
     */
    private void updateSprintIfChanged(Sprint sprint, SprintDTO sprintDTO) {
        updatePropertyIfChanged(sprint::getName, sprint::setName, sprintDTO.getName());
        updatePropertyIfChanged(sprint::getState, sprint::setState, sprintDTO.getState());
        updatePropertyIfChanged(sprint::getStartDate, sprint::setStartDate, sprintDTO.getStartDate());
        updatePropertyIfChanged(sprint::getEndDate, sprint::setEndDate, sprintDTO.getEndDate());
        updatePropertyIfChanged(sprint::getCompleteDate, sprint::setCompleteDate, sprintDTO.getCompleteDate());
        updatePropertyIfChanged(sprint::getGoal, sprint::setGoal, sprintDTO.getGoal());
    }

    /**
     * Atualiza uma propriedade se o valor for diferente do atual
     * @param getter Função para obter o valor atual
     * @param setter Função para atualizar o valor
     * @param newValue Novo valor
     * @param <T> Tipo do valor
     */
    private <T> void updatePropertyIfChanged(Supplier<T> getter, Consumer<T> setter, T newValue) {
        if (nonNull(newValue) && !Objects.equals(getter.get(), newValue)) {
            setter.accept(newValue);
        }
    }

    /**
     * Busca um sprint no banco de dados, caso não exista, cria um novo
     * @param sprintId Identificador da sprint
     * @return
     */
    public SprintDTO getSprintDTO(Long sprintId) {
        return toDto(findById(sprintId));
    }

}
