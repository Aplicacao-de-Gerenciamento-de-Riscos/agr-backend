package com.catolicasc.agrbackend.feature.component.repository;

import com.catolicasc.agrbackend.feature.component.domain.Component;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComponentRepository extends JpaRepository<Component, Long> {
}
