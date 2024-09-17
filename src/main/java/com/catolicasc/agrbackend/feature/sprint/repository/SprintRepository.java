package com.catolicasc.agrbackend.feature.sprint.repository;

import com.catolicasc.agrbackend.feature.sprint.domain.Sprint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SprintRepository extends JpaRepository<Sprint, Long> {
}
