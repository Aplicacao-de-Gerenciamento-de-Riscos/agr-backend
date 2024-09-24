package com.catolicasc.agrbackend.feature.worklog.repository;

import com.catolicasc.agrbackend.feature.worklog.domain.Worklog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorklogRepository extends JpaRepository<Worklog, Long> {
}
