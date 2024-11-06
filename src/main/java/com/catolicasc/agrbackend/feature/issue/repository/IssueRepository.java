package com.catolicasc.agrbackend.feature.issue.repository;

import com.catolicasc.agrbackend.feature.issue.domain.Issue;
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
}
