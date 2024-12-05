package com.catolicasc.agrbackend.feature.sprint.service;

import com.catolicasc.agrbackend.clients.jira.dto.JiraSprintResponseDTO;
import com.catolicasc.agrbackend.clients.jira.service.JiraAPI;
import com.catolicasc.agrbackend.feature.sprint.domain.Sprint;
import com.catolicasc.agrbackend.feature.sprint.dto.SprintDTO;
import com.catolicasc.agrbackend.feature.sprint.repository.SprintRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SprintServiceTest {

    @InjectMocks
    private SprintService sprintService;

    @Mock
    private SprintRepository sprintRepository;

    @Mock
    private JiraAPI jiraAPI;

    private Sprint sprint;
    private SprintDTO sprintDTO;
    private JiraSprintResponseDTO.SprintResponse sprintResponse;

    @BeforeEach
    void setUp() {
        // Prepare mock Sprint and SprintDTO
        sprint = new Sprint();
        sprint.setId(1L);
        sprint.setName("Sprint 1");

        sprintDTO = new SprintDTO();
        sprintDTO.setId(1L);
        sprintDTO.setName("Sprint 1");
        sprintDTO.setStartDate(LocalDateTime.now().atOffset(ZoneOffset.UTC).toLocalDateTime());
        sprintDTO.setEndDate(LocalDateTime.now().plusDays(1).atOffset(ZoneOffset.UTC).toLocalDateTime());

        sprintResponse = new JiraSprintResponseDTO.SprintResponse();
        sprintResponse.setId("1");
        sprintResponse.setName("Sprint 1");
        sprintResponse.setStartDate("2024-12-01T00:00:00.000+0000");
        sprintResponse.setEndDate("2024-12-02T00:00:00.000+0000");
    }

    @Test
    void testFindById() {
        // Mocking the repository to return a valid sprint
        when(sprintRepository.findById(1L)).thenReturn(Optional.of(sprint));

        Sprint foundSprint = sprintService.findById(1L);

        // Verifying the mock call
        verify(sprintRepository, times(1)).findById(1L);

        // Asserting the results
        assertNotNull(foundSprint);
        assertEquals("Sprint 1", foundSprint.getName());
    }

    @Test
    void testFindByIdNotFound() {
        // Mocking the repository to return empty
        when(sprintRepository.findById(1L)).thenReturn(Optional.empty());

        Sprint foundSprint = sprintService.findById(1L);

        // Verifying the mock call
        verify(sprintRepository, times(1)).findById(1L);

        // Asserting that no sprint was found
        assertNull(foundSprint);
    }

    @Test
    void testFindAll() {
        // Mocking the repository to return a list with one sprint
        when(sprintRepository.findAll()).thenReturn(List.of(sprint));

        List<Sprint> allSprints = sprintService.findAll();

        // Asserting the results
        assertFalse(allSprints.isEmpty());
        assertEquals(1, allSprints.size());
    }

    @Test
    void testSyncSprintsByBoard() {
        JiraSprintResponseDTO jiraSprintResponseDTO = new JiraSprintResponseDTO();
        jiraSprintResponseDTO.setValues(List.of(sprintResponse));
        jiraSprintResponseDTO.setLast(true);

        ResponseEntity<JiraSprintResponseDTO> responseEntity = ResponseEntity.ok(jiraSprintResponseDTO);

        when(jiraAPI.listSprintsByBoard("1", 0L)).thenReturn(responseEntity);

        // Verificação do que está sendo retornado
        ResponseEntity<JiraSprintResponseDTO> result = jiraAPI.listSprintsByBoard("1", 0L);

        when(sprintRepository.findById(1L)).thenReturn(Optional.empty());
        when(sprintRepository.save(any(Sprint.class))).thenReturn(sprint);

        sprintService.syncSprintsByBoard("1");

        verify(jiraAPI, times(2)).listSprintsByBoard("1", 0L);
        verify(sprintRepository, times(1)).save(any(Sprint.class));
    }

    @Test
    void testToDomain() {
        Sprint sprintDomain = sprintService.toDomain(sprintDTO);

        // Asserting that the domain object is correctly mapped from DTO
        assertNotNull(sprintDomain);
        assertEquals(sprintDTO.getId(), sprintDomain.getId());
        assertEquals(sprintDTO.getName(), sprintDomain.getName());
    }

    @Test
    void testToDto() {
        SprintDTO dto = sprintService.toDto(sprint);

        // Asserting that the DTO is correctly mapped from domain
        assertNotNull(dto);
        assertEquals(sprint.getId(), dto.getId());
        assertEquals(sprint.getName(), dto.getName());
    }

    @Test
    void testUpdateSprintIfChanged() {
        Sprint updatedSprint = new Sprint();
        updatedSprint.setId(1L);
        updatedSprint.setName("Updated Sprint");

        SprintDTO updatedDTO = new SprintDTO();
        updatedDTO.setName("Updated Sprint");

        sprintService.updateSprintIfChanged(sprint, updatedDTO);

        // Asserting that the sprint name was updated
        assertEquals("Updated Sprint", sprint.getName());
    }

    @Test
    void testGetSprintDTO() {
        // Mocking repository to return a sprint by ID
        when(sprintRepository.findById(1L)).thenReturn(Optional.of(sprint));

        SprintDTO result = sprintService.getSprintDTO(1L);

        // Asserting that the result is not null and matches the expected values
        assertNotNull(result);
        assertEquals(sprint.getId(), result.getId());
        assertEquals(sprint.getName(), result.getName());
    }
}