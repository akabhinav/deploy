package com.paas.core.service;

import com.paas.common.enums.DatabaseType;
import com.paas.core.entity.ManagedDatabase;
import com.paas.core.repository.ManagedDatabaseRepository;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.api.model.apps.StatefulSetBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DatabaseProvisioningService {

    private final ManagedDatabaseRepository databaseRepository;

    @Transactional
    public ManagedDatabase provisionDatabase(
            String name,
            DatabaseType type,
            String version,
            Long projectId,
            Long environmentId,
            Integer storageSizeGb,
            Boolean backupEnabled,
            Integer backupRetentionDays,
            Boolean highAvailability) {

        log.info("Provisioning database: {} of type: {}", name, type);

        if (databaseRepository.existsByName(name)) {
            throw new RuntimeException("Database already exists with name: " + name);
        }

        // Generate credentials
        String username = generateUsername(name);
        String password = generatePassword();

        ManagedDatabase database = ManagedDatabase.builder()
                .name(name)
                .databaseType(type)
                .version(version)
                .projectId(projectId)
                .environmentId(environmentId)
                .username(username)
                .passwordSecret(password)
                .storageSizeGb(storageSizeGb != null ? storageSizeGb : 10)
                .backupEnabled(backupEnabled != null ? backupEnabled : true)
                .backupRetentionDays(backupRetentionDays != null ? backupRetentionDays : 7)
                .highAvailability(highAvailability != null ? highAvailability : false)
                .status("PROVISIONING")
                .build();

        database = databaseRepository.save(database);

        // Deploy to Kubernetes (async)
        final Long dbId = database.getId();
        deployDatabaseAsync(dbId, name, type, version, storageSizeGb);

        return database;
    }

    private void deployDatabaseAsync(Long dbId, String name, DatabaseType type, String version, Integer storageSizeGb) {
        new Thread(() -> {
            try {
                deployToKubernetes(dbId, name, type, version, storageSizeGb);
                updateDatabaseStatus(dbId, "RUNNING");
            } catch (Exception e) {
                log.error("Failed to deploy database: {}", e.getMessage(), e);
                updateDatabaseStatus(dbId, "FAILED");
            }
        }).start();
    }

    private void deployToKubernetes(Long dbId, String name, DatabaseType type, String version, Integer storageSizeGb) {
        log.info("Deploying database {} to Kubernetes", name);

        try (KubernetesClient client = new KubernetesClientBuilder().build()) {

            String namespace = "databases";
            ensureNamespaceExists(client, namespace);

            // Create StatefulSet based on database type
            StatefulSet statefulSet = createStatefulSet(name, type, version, storageSizeGb);
            client.apps().statefulSets()
                    .inNamespace(namespace)
                    .createOrReplace(statefulSet);

            // Create Service
            Service service = createDatabaseService(name);
            client.services()
                    .inNamespace(namespace)
                    .createOrReplace(service);

            // Update connection details
            updateConnectionDetails(dbId, name, namespace);

            log.info("Database deployed successfully: {}", name);

        } catch (Exception e) {
            log.error("Kubernetes deployment failed: {}", e.getMessage(), e);
            throw new RuntimeException("Database deployment failed", e);
        }
    }

    @Transactional(readOnly = true)
    public ManagedDatabase getDatabaseById(Long id) {
        return databaseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Database not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<ManagedDatabase> getDatabasesByProject(Long projectId) {
        return databaseRepository.findByProjectId(projectId);
    }

    @Transactional(readOnly = true)
    public List<ManagedDatabase> getAllDatabases() {
        return databaseRepository.findAll();
    }

    @Transactional
    public void deleteDatabase(Long id) {
        log.info("Deleting database: {}", id);

        ManagedDatabase database = getDatabaseById(id);

        // Delete from Kubernetes
        try (KubernetesClient client = new KubernetesClientBuilder().build()) {
            client.apps().statefulSets()
                    .inNamespace("databases")
                    .withName(database.getName())
                    .delete();

            client.services()
                    .inNamespace("databases")
                    .withName(database.getName())
                    .delete();
        } catch (Exception e) {
            log.error("Failed to delete from Kubernetes: {}", e.getMessage());
        }

        databaseRepository.deleteById(id);
        log.info("Database deleted successfully");
    }

    private StatefulSet createStatefulSet(String name, DatabaseType type, String version, Integer storageSizeGb) {
        Map<String, String> labels = Map.of("app", name, "type", "database");

        String image = getDockerImage(type, version);
        int port = getDatabasePort(type);

        return new StatefulSetBuilder()
                .withNewMetadata()
                    .withName(name)
                    .withLabels(labels)
                .endMetadata()
                .withNewSpec()
                    .withReplicas(1)
                    .withServiceName(name)
                    .withNewSelector()
                        .withMatchLabels(labels)
                    .endSelector()
                    .withNewTemplate()
                        .withNewMetadata()
                            .withLabels(labels)
                        .endMetadata()
                        .withNewSpec()
                            .addNewContainer()
                                .withName(name)
                                .withImage(image)
                                .addNewPort()
                                    .withContainerPort(port)
                                .endPort()
                                .addNewVolumeMount()
                                    .withName("data")
                                    .withMountPath(getDataPath(type))
                                .endVolumeMount()
                            .endContainer()
                        .endSpec()
                    .endTemplate()
                    .addNewVolumeClaimTemplate()
                        .withNewMetadata()
                            .withName("data")
                        .endMetadata()
                        .withNewSpec()
                            .withAccessModes("ReadWriteOnce")
                            .withNewResources()
                                .withRequests(Map.of("storage", new Quantity(storageSizeGb + "Gi")))
                            .endResources()
                        .endSpec()
                    .endVolumeClaimTemplate()
                .endSpec()
                .build();
    }

    private Service createDatabaseService(String name) {
        Map<String, String> labels = Map.of("app", name);

        return new ServiceBuilder()
                .withNewMetadata()
                    .withName(name)
                    .withLabels(labels)
                .endMetadata()
                .withNewSpec()
                    .withSelector(labels)
                    .withType("ClusterIP")
                    .addNewPort()
                        .withPort(5432)
                        .withTargetPort(new IntOrString(5432))
                    .endPort()
                .endSpec()
                .build();
    }

    private void ensureNamespaceExists(KubernetesClient client, String namespace) {
        Namespace ns = client.namespaces().withName(namespace).get();
        if (ns == null) {
            client.namespaces().create(new NamespaceBuilder()
                    .withNewMetadata()
                    .withName(namespace)
                    .endMetadata()
                    .build());
        }
    }

    private void updateConnectionDetails(Long dbId, String name, String namespace) {
        ManagedDatabase database = databaseRepository.findById(dbId)
                .orElseThrow(() -> new RuntimeException("Database not found"));

        String host = name + "." + namespace + ".svc.cluster.local";
        int port = getDatabasePort(database.getDatabaseType());

        database.setHost(host);
        database.setPort(port);
        database.setDatabaseName(name);
        database.setConnectionString(
                String.format("jdbc:%s://%s:%d/%s",
                        database.getDatabaseType().name().toLowerCase(),
                        host, port, name)
        );

        databaseRepository.save(database);
    }

    private void updateDatabaseStatus(Long dbId, String status) {
        databaseRepository.findById(dbId).ifPresent(db -> {
            db.setStatus(status);
            databaseRepository.save(db);
        });
    }

    private String getDockerImage(DatabaseType type, String version) {
        return switch (type) {
            case POSTGRESQL -> "postgres:" + version;
            case MYSQL -> "mysql:" + version;
            case MONGODB -> "mongo:" + version;
            case REDIS -> "redis:" + version;
            case ELASTICSEARCH -> "elasticsearch:" + version;
            case CASSANDRA -> "cassandra:" + version;
            case MARIADB -> "mariadb:" + version;
        };
    }

    private int getDatabasePort(DatabaseType type) {
        return switch (type) {
            case POSTGRESQL -> 5432;
            case MYSQL, MARIADB -> 3306;
            case MONGODB -> 27017;
            case REDIS -> 6379;
            case ELASTICSEARCH -> 9200;
            case CASSANDRA -> 9042;
        };
    }

    private String getDataPath(DatabaseType type) {
        return switch (type) {
            case POSTGRESQL -> "/var/lib/postgresql/data";
            case MYSQL, MARIADB -> "/var/lib/mysql";
            case MONGODB -> "/data/db";
            case REDIS -> "/data";
            case ELASTICSEARCH -> "/usr/share/elasticsearch/data";
            case CASSANDRA -> "/var/lib/cassandra";
        };
    }

    private String generateUsername(String dbName) {
        return dbName.replaceAll("[^a-z0-9]", "") + "_user";
    }

    private String generatePassword() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
