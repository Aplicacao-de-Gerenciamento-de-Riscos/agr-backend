package com.catolicasc.agrbackend.feature.epic.repository;

import com.catolicasc.agrbackend.feature.epic.domain.Epic;
import com.catolicasc.agrbackend.feature.epic.dto.EpicUsageDTO;
import com.catolicasc.agrbackend.feature.issue.dto.CriticalIssueRelationDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EpicRepository extends JpaRepository<Epic, Long> {
    /**
     * Recupera os épicos e seu alocável em horas por versão.
     *
     * @param codVersions uma lista de IDs de versões para filtrar (pode ser nulo)
     * @return uma lista de CriticalIssueRelationDTO, cada um correspondendo a uma versão ou sprint
     */
    @Query("SELECT new com.catolicasc.agrbackend.feature.epic.dto.EpicUsageDTO(" +
            "CAST(vi.version.id AS Long), CAST(e.id AS Long), e.name, e.summary, e.key, (SUM(i.timespent) / 3600)) " +
            "FROM Epic e " +
            "LEFT JOIN Issue i ON e.id = i.epic.id " +
            "LEFT JOIN VersionIssue vi ON i.id = vi.issue.id " +
            "WHERE vi.version.id IN :codVersions " +
            "GROUP BY vi.version.id, e.id"
    )
    List<EpicUsageDTO> findEpicUsagesByVersions(@Param("codVersions") List<Long> codVersions);
}
