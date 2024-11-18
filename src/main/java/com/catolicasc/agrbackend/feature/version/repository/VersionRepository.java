package com.catolicasc.agrbackend.feature.version.repository;

import com.catolicasc.agrbackend.feature.version.domain.Version;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VersionRepository extends JpaRepository<Version, Long> {
    List<Version> findAllByProjectId(Long projectId);
}
