package com.catolicasc.agrbackend.feature.epic.repository;

import com.catolicasc.agrbackend.feature.epic.domain.Epic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EpicRepository extends JpaRepository<Epic, Long> {
}
