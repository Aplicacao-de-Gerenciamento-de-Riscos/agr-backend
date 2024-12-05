package com.catolicasc.agrbackend.feature.version.service;

import com.catolicasc.agrbackend.clients.jira.dto.JiraIssueResponseDTO;
import com.catolicasc.agrbackend.clients.jira.dto.JiraVersionResponseDTO;
import com.catolicasc.agrbackend.feature.project.domain.Project;
import com.catolicasc.agrbackend.feature.project.service.ProjectService;
import com.catolicasc.agrbackend.feature.version.domain.Version;
import com.catolicasc.agrbackend.feature.version.dto.VersionDTO;
import com.catolicasc.agrbackend.feature.version.repository.VersionRepository;
import com.catolicasc.agrbackend.clients.jira.service.JiraAPI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VersionServiceTest {

    @InjectMocks
    private VersionService versionService;

    @Mock
    private VersionRepository versionRepository;

    @Mock
    private JiraAPI jiraAPI;

    @Mock
    private ProjectService projectService;

    private Version version;
    private VersionDTO versionDTO;
    private JiraVersionResponseDTO jiraVersionResponseDTO;

    @BeforeEach
    void setUp() {
        // Preparando o mock para versão e DTO
        version = new Version();
        version.setId(1L);
        version.setName("Version 1");
        version.setReleaseDate(LocalDate.now());

        versionDTO = new VersionDTO();
        versionDTO.setId(1L);
        versionDTO.setName("Version 1");
        versionDTO.setReleaseDate(LocalDate.now());

        jiraVersionResponseDTO = new JiraVersionResponseDTO();
        jiraVersionResponseDTO.setId("1");
        jiraVersionResponseDTO.setName("Version 1");
        jiraVersionResponseDTO.setReleaseDate("2024-12-01");
    }

    @Test
    void testFindById() {
        // Mocking o repositório para retornar uma versão válida
        when(versionRepository.findById(1L)).thenReturn(Optional.of(version));

        Version foundVersion = versionService.findById(1L);

        // Verificando se o método findById foi chamado corretamente
        verify(versionRepository, times(1)).findById(1L);

        // Verificando o resultado
        assertNotNull(foundVersion);
        assertEquals("Version 1", foundVersion.getName());
    }

    @Test
    void testFindByIdNotFound() {
        // Mocking o repositório para retornar uma versão não encontrada
        when(versionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> versionService.findById(1L));

        // Verificando se o repositório foi chamado corretamente
        verify(versionRepository, times(1)).findById(1L);
    }

    @Test
    void testFindAllByProjectId() {
        // Preparando dados para o teste
        List<Long> projectIds = List.of(1L);
        when(versionRepository.findAllByProjectId(1L)).thenReturn(List.of(version));

        List<VersionDTO> versions = versionService.findAllByProjectId(projectIds);

        // Verificando se a lista de versões não está vazia e contém o elemento correto
        assertFalse(versions.isEmpty());
        assertEquals(1, versions.size());
        assertEquals("Version 1", versions.get(0).getName());
    }

    @Test
    void testSyncVersions() {
        // Preparando o mock para as versões do Jira
        JiraVersionResponseDTO jiraVersionResponse = new JiraVersionResponseDTO();
        jiraVersionResponse.setId("1");
        jiraVersionResponse.setName("Version 1");
        jiraVersionResponse.setReleaseDate("2024-12-01");

        ResponseEntity<List<JiraVersionResponseDTO>> responseEntity = ResponseEntity.ok(List.of(jiraVersionResponse));

        // Mocking a chamada da API Jira
        when(jiraAPI.listVersionsByProject("PROJECT_KEY")).thenReturn(responseEntity);

        Project project = new Project();
        project.setKey("PROJECT_KEY");

        // Mocking o repositório para retornar uma versão já existente
        when(versionRepository.findById(1L)).thenReturn(Optional.of(version));
        when(versionRepository.save(any(Version.class))).thenReturn(version);

        // Chamando o método syncVersions
        versionService.syncVersions(List.of(project));

        // Verificando as chamadas feitas
        verify(jiraAPI, times(1)).listVersionsByProject("PROJECT_KEY");
        verify(versionRepository, times(1)).save(any(Version.class));
    }

    @Test
    void testToDTO() {
        VersionDTO dto = versionService.toDTO(version);

        // Verificando se o DTO foi mapeado corretamente
        assertNotNull(dto);
        assertEquals(version.getId(), dto.getId());
        assertEquals(version.getName(), dto.getName());
    }

    @Test
    void testToDomain() {
        Version versionDomain = versionService.toDomain(versionDTO);

        // Verificando se o objeto foi convertido corretamente
        assertNotNull(versionDomain);
        assertEquals(versionDTO.getId(), versionDomain.getId());
        assertEquals(versionDTO.getName(), versionDomain.getName());
    }

    @Test
    void testUpdateVersionIfChanged() {
        VersionDTO updatedDTO = new VersionDTO();
        updatedDTO.setName("Updated Version");
        updatedDTO.setDescription("Updated description");

        // Atualizando a versão
        versionService.updateVersionIfChanged(version, updatedDTO);

        // Verificando se os atributos da versão foram atualizados corretamente
        assertEquals("Updated Version", version.getName());
        assertEquals("Updated description", version.getDescription());
    }

    @Test
    void testGetVersionDTO() {
        when(versionRepository.findById(1L)).thenReturn(Optional.of(version));

        VersionDTO result = versionService.getVersionDTO(1L);

        // Verificando se o DTO foi retornado corretamente
        assertNotNull(result);
        assertEquals(version.getId(), result.getId());
        assertEquals(version.getName(), result.getName());
    }
}
