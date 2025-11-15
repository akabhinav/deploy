package com.paas.core.repository;

import com.paas.core.entity.Environment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EnvironmentRepository extends JpaRepository<Environment, Long> {

    Optional<Environment> findByName(String name);

    Optional<Environment> findByNamespace(String namespace);

    boolean existsByName(String name);

    boolean existsByNamespace(String namespace);
}
