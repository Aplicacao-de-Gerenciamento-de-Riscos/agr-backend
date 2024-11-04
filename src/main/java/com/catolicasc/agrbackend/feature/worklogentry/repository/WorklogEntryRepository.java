package com.catolicasc.agrbackend.feature.worklogentry.repository;

import com.catolicasc.agrbackend.feature.worklog.domain.Worklog;
import com.catolicasc.agrbackend.feature.worklogentry.domain.WorklogEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorklogEntryRepository extends JpaRepository<WorklogEntry, Long> {
    List<WorklogEntry> findByWorklogId(Long worklogId);
    void deleteAllByWorklog(Worklog worklog);
}
