package com.catolicasc.agrbackend.feature.worklogentry.repository;

import com.catolicasc.agrbackend.feature.worklogentry.domain.WorklogEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorklogEntryRepository extends JpaRepository<WorklogEntry, Long> {
}
