package com.paas.core.repository;

import com.paas.core.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    Optional<Application> findByName(String name);

    List<Application> findByEnvironmentId(Long environmentId);

    List<Application> findByStatus(String status);

    boolean existsByName(String name);
}
