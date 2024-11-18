package com.catolicasc.agrbackend.feature.issue.repository;

import com.catolicasc.agrbackend.feature.issue.domain.Issue;
import com.catolicasc.agrbackend.feature.issue.dto.BugIssuesRelationDTO;
import com.catolicasc.agrbackend.feature.issue.dto.CriticalIssueRelationDTO;
import com.catolicasc.agrbackend.feature.issue.dto.IssuePlanningDTO;
import com.catolicasc.agrbackend.feature.issue.dto.IssueTypeDonePercentageDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IssueRepository extends JpaRepository<Issue, Long> {

    /**
     * Recupera a porcentagem de issues marcadas como 'DONE' para cada tipo de issue.
     *
     * @param codVersion o ID da versão para filtrar (pode ser nulo)
     * @param codSprint o ID do sprint para filtrar (pode ser nulo)
     * @return uma lista de IssueTypeDonePercentageDTO contendo o tipo de issue e a porcentagem de issues 'DONE'
     */
    @Query("SELECT new com.catolicasc.agrbackend.feature.issue.dto.IssueTypeDonePercentageDTO(i.issueType, " +
            "(SUM(CASE WHEN i.status = 'Done' THEN 1 ELSE 0 END) * 100.0 / COUNT(i))) " +
            "FROM Issue i " +
            "LEFT JOIN VersionIssue vi ON i.id = vi.issue.id " +
            "WHERE (:codVersion IS NULL OR vi.version.id = :codVersion) " +
            "AND (:codSprint IS NULL OR i.id = :codSprint) " +
            "GROUP BY i.issueType")
    List<IssueTypeDonePercentageDTO> findDonePercentageByIssueType(@Param("codVersion") Long codVersion,
                                                                   @Param("codSprint") Long codSprint);

    /**
     * Recupera a quantidade de issues marcadas como 'DONE', a quantidade de issues marcadas em qualquer outro estado, e a quantidade de issues totais por sprint ou versão.
     *
     * @param codVersion o ID da versão para filtrar (pode ser nulo)
     * @param codSprint o ID do sprint para filtrar (pode ser nulo)
     * @return um objeto WorkPlanningVarianceDTO contendo as quantidades de issues
     */
    @Query("SELECT new com.catolicasc.agrbackend.feature.issue.dto.IssuePlanningDTO(" +
            "SUM(CASE WHEN i.status = 'Done' THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN i.status != 'Done' THEN 1 ELSE 0 END), " +
            "COUNT(i)) " +
            "FROM Issue i " +
            "LEFT JOIN VersionIssue vi ON i.id = vi.issue.id " +
            "WHERE (:codVersion IS NULL OR vi.version.id = :codVersion) " +
            "AND (:codSprint IS NULL OR i.id = :codSprint)")
    IssuePlanningDTO findWorkPlanningVariance(@Param("codVersion") Long codVersion,
                                              @Param("codSprint") Long codSprint);

    /**
     * Recupera a quantidade de issues críticas (CRITICAL ou BLOCKER) e não críticas por sprint ou versão.
     * Também recupera a quantidade total de issues.
     * Calcula a porcentagem de issues críticas e não críticas.
     *
     * @param codVersions uma lista de IDs de versões para filtrar (pode ser nulo)
     * @return uma lista de CriticalIssueRelationDTO, cada um correspondendo a uma versão ou sprint
     */
    @Query("SELECT new com.catolicasc.agrbackend.feature.issue.dto.CriticalIssueRelationDTO(" +
            "CAST(vi.version.id AS long), " +
            "SUM(CASE WHEN i.priority = 'Critical' OR i.priority = 'Blocker' THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN i.priority != 'Critical' AND i.priority != 'Blocker' THEN 1 ELSE 0 END), " +
            "COUNT(DISTINCT i), " +
            "CAST(SUM(CASE WHEN i.priority = 'Critical' OR i.priority = 'Blocker' THEN 1 ELSE 0 END) * 100.0 / COUNT(DISTINCT i) AS double), " +
            "CAST(SUM(CASE WHEN i.priority != 'Critical' AND i.priority != 'Blocker' THEN 1 ELSE 0 END) * 100.0 / COUNT(DISTINCT i) AS double)) " +
            "FROM Issue i " +
            "LEFT JOIN VersionIssue vi ON i.id = vi.issue.id " +
            "WHERE (:codVersions IS NULL OR vi.version.id IN :codVersions) " +
            "AND vi.version.id IS NOT NULL " + // Garante que apenas registros válidos sejam considerados
            "GROUP BY vi.version.id")
    List<CriticalIssueRelationDTO> findCriticalIssuesByVersions(@Param("codVersions") List<Long> codVersions);

    /**
     * Recupera a quantidade de issues críticas (CRITICAL ou BLOCKER) e não críticas por sprint ou versão.
     * Também recupera a quantidade total de issues.
     * Calcula a porcentagem de issues críticas e não críticas.
     *
     * @param codSprints uma lista de IDs de versões para filtrar (pode ser nulo)
     * @return uma lista de CriticalIssueRelationDTO, cada um correspondendo a uma versão ou sprint
     */
    @Query("SELECT new com.catolicasc.agrbackend.feature.issue.dto.CriticalIssueRelationDTO(" +
            "CAST(i.sprint.id AS long), " +
            "SUM(CASE WHEN i.priority = 'Critical' OR i.priority = 'Blocker' THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN i.priority != 'Critical' AND i.priority != 'Blocker' THEN 1 ELSE 0 END), " +
            "COUNT(DISTINCT i), " +
            "CAST(SUM(CASE WHEN i.priority = 'Critical' OR i.priority = 'Blocker' THEN 1 ELSE 0 END) * 100.0 / COUNT(DISTINCT i) AS double), " +
            "CAST(SUM(CASE WHEN i.priority != 'Critical' AND i.priority != 'Blocker' THEN 1 ELSE 0 END) * 100.0 / COUNT(DISTINCT i) AS double)) " +
            "FROM Issue i " +
            "LEFT JOIN VersionIssue vi ON i.id = vi.issue.id " +
            "WHERE (:codSprints IS NULL OR i.sprint.id IN :codSprints) " +
            "AND i.sprint.id IS NOT NULL " + // Garante que apenas registros válidos sejam considerados
            "GROUP BY i.sprint.id")
    List<CriticalIssueRelationDTO> findCriticalIssuesBySprints(@Param("codSprints") List<Long> codSprints);

    /**
     * Recupera a quantidade de issues do tipo 'BUG' e não 'BUG' por sprint ou versão.
     * Também recupera a quantidade total de issues.
     * Calcula a porcentagem de issues do tipo 'BUG' e não 'BUG'.
     *
     * @param codVersions uma lista de IDs de versões para filtrar (pode ser nulo)
     * @return uma lista de BugIssuesRelationDTO, cada um correspondendo a uma versão ou sprint
     */
    @Query("SELECT new com.catolicasc.agrbackend.feature.issue.dto.BugIssuesRelationDTO(" +
            "CAST(vi.version.id AS long), " + // Garante que o ID seja Long
            "SUM(CASE WHEN i.issueType = 'Bug' THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN i.issueType != 'Bug' THEN 1 ELSE 0 END), " +
            "COUNT(i), " +
            "CAST((SUM(CASE WHEN i.issueType = 'Bug' THEN 1 ELSE 0 END) * 100.0 / COUNT(i)) AS double), " +
            "CAST((SUM(CASE WHEN i.issueType != 'Bug' THEN 1 ELSE 0 END) * 100.0 / COUNT(i)) AS double)) " +
            "FROM Issue i " +
            "LEFT JOIN VersionIssue vi ON i.id = vi.issue.id " +
            "WHERE (:codVersions IS NULL OR vi.version.id IN :codVersions) " +
            "AND vi.version.id IS NOT NULL " + // Garante que apenas registros válidos sejam considerados
            "GROUP BY vi.version.id")
    List<BugIssuesRelationDTO> findBugIssuesByVersions(@Param("codVersions") List<Long> codVersions);

    /**
     * Recupera a quantidade de issues do tipo 'BUG' e não 'BUG' por sprint ou versão.
     * Também recupera a quantidade total de issues.
     * Calcula a porcentagem de issues do tipo 'BUG' e não 'BUG'.
     *
     * @param codSprints uma lista de IDs de versões para filtrar (pode ser nulo)
     * @return uma lista de BugIssuesRelationDTO, cada um correspondendo a uma versão ou sprint
     */
    @Query("SELECT new com.catolicasc.agrbackend.feature.issue.dto.BugIssuesRelationDTO(" +
            "CAST(i.sprint.id AS long), " + // Garante que o ID seja Long
            "SUM(CASE WHEN i.issueType = 'Bug' THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN i.issueType != 'Bug' THEN 1 ELSE 0 END), " +
            "COUNT(i), " +
            "CAST((SUM(CASE WHEN i.issueType = 'Bug' THEN 1 ELSE 0 END) * 100.0 / COUNT(i)) AS double), " +
            "CAST((SUM(CASE WHEN i.issueType != 'Bug' THEN 1 ELSE 0 END) * 100.0 / COUNT(i)) AS double)) " +
            "FROM Issue i " +
            "LEFT JOIN VersionIssue vi ON i.id = vi.issue.id " +
            "WHERE (:codSprints IS NULL OR i.sprint.id IN :codSprints) " +
            "AND i.sprint.id IS NOT NULL " + // Garante que apenas registros válidos sejam considerados
            "GROUP BY i.sprint.id")
    List<BugIssuesRelationDTO> findBugIssuesBySprints(@Param("codSprints") List<Long> codSprints);

}
