package com.catolicasc.agrbackend.feature.versionissue.repository;

import com.catolicasc.agrbackend.feature.versionissue.domain.VersionIssue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VersionIssueRepository extends JpaRepository<VersionIssue, Long> {
}
