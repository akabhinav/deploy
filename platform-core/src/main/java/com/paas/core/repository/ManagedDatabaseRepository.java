package com.paas.core.repository;

import com.paas.common.enums.DatabaseType;
import com.paas.core.entity.ManagedDatabase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ManagedDatabaseRepository extends JpaRepository<ManagedDatabase, Long> {

    Optional<ManagedDatabase> findByName(String name);

    List<ManagedDatabase> findByProjectId(Long projectId);

    List<ManagedDatabase> findByEnvironmentId(Long environmentId);

    List<ManagedDatabase> findByDatabaseType(DatabaseType databaseType);

    boolean existsByName(String name);
}
