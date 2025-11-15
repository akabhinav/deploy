package com.paas.core.service;

import com.paas.core.entity.Application;
import com.paas.core.entity.AutoScalingConfig;
import com.paas.core.entity.Deployment;
import com.paas.core.repository.ApplicationRepository;
import com.paas.core.repository.AutoScalingConfigRepository;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.api.model.autoscaling.v2.HorizontalPodAutoscaler;
import io.fabric8.kubernetes.api.model.autoscaling.v2.HorizontalPodAutoscalerBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class KubernetesDeploymentService {

    private final ApplicationRepository applicationRepository;
    private final AutoScalingConfigRepository autoScalingConfigRepository;

    /**
     * Deploy application to Kubernetes cluster
     */
    public void deployToKubernetes(Long applicationId, String namespace) {
        log.info("Deploying application {} to Kubernetes namespace: {}", applicationId, namespace);

        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found: " + applicationId));

        try (KubernetesClient client = new KubernetesClientBuilder().build()) {

            // Create or update namespace
            ensureNamespaceExists(client, namespace);

            // Create deployment
            io.fabric8.kubernetes.api.model.apps.Deployment k8sDeployment = createKubernetesDeployment(app, namespace);
            client.apps().deployments()
                    .inNamespace(namespace)
                    .createOrReplace(k8sDeployment);

            log.info("Deployment created: {}", app.getName());

            // Create service
            Service service = createKubernetesService(app, namespace);
            client.services()
                    .inNamespace(namespace)
                    .createOrReplace(service);

            log.info("Service created: {}", app.getName());

            // Create HPA if auto-scaling is enabled
            AutoScalingConfig autoScaling = autoScalingConfigRepository.findByApplicationId(applicationId)
                    .orElse(null);

            if (autoScaling != null && autoScaling.getEnabled()) {
                HorizontalPodAutoscaler hpa = createHorizontalPodAutoscaler(app, autoScaling, namespace);
                client.autoscaling().v2().horizontalPodAutoscalers()
                        .inNamespace(namespace)
                        .createOrReplace(hpa);
                log.info("HPA created for: {}", app.getName());
            }

            log.info("Successfully deployed {} to Kubernetes", app.getName());

        } catch (Exception e) {
            log.error("Failed to deploy to Kubernetes: {}", e.getMessage(), e);
            throw new RuntimeException("Kubernetes deployment failed: " + e.getMessage(), e);
        }
    }

    /**
     * Delete application from Kubernetes
     */
    public void deleteFromKubernetes(Long applicationId, String namespace) {
        log.info("Deleting application {} from Kubernetes", applicationId);

        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found: " + applicationId));

        try (KubernetesClient client = new KubernetesClientBuilder().build()) {

            String appName = app.getName();

            // Delete deployment
            client.apps().deployments()
                    .inNamespace(namespace)
                    .withName(appName)
                    .delete();

            // Delete service
            client.services()
                    .inNamespace(namespace)
                    .withName(appName)
                    .delete();

            // Delete HPA if exists
            client.autoscaling().v2().horizontalPodAutoscalers()
                    .inNamespace(namespace)
                    .withName(appName + "-hpa")
                    .delete();

            log.info("Successfully deleted {} from Kubernetes", appName);

        } catch (Exception e) {
            log.error("Failed to delete from Kubernetes: {}", e.getMessage(), e);
            throw new RuntimeException("Kubernetes deletion failed: " + e.getMessage(), e);
        }
    }

    /**
     * Scale application replicas
     */
    public void scaleApplication(Long applicationId, String namespace, int replicas) {
        log.info("Scaling application {} to {} replicas", applicationId, replicas);

        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found: " + applicationId));

        try (KubernetesClient client = new KubernetesClientBuilder().build()) {

            client.apps().deployments()
                    .inNamespace(namespace)
                    .withName(app.getName())
                    .scale(replicas);

            log.info("Scaled {} to {} replicas", app.getName(), replicas);

        } catch (Exception e) {
            log.error("Failed to scale application: {}", e.getMessage(), e);
            throw new RuntimeException("Scaling failed: " + e.getMessage(), e);
        }
    }

    /**
     * Get deployment status from Kubernetes
     */
    public Map<String, Object> getDeploymentStatus(Long applicationId, String namespace) {
        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found: " + applicationId));

        Map<String, Object> status = new HashMap<>();

        try (KubernetesClient client = new KubernetesClientBuilder().build()) {

            io.fabric8.kubernetes.api.model.apps.Deployment deployment = client.apps().deployments()
                    .inNamespace(namespace)
                    .withName(app.getName())
                    .get();

            if (deployment != null) {
                status.put("name", deployment.getMetadata().getName());
                status.put("replicas", deployment.getSpec().getReplicas());
                status.put("availableReplicas", deployment.getStatus().getAvailableReplicas());
                status.put("readyReplicas", deployment.getStatus().getReadyReplicas());
                status.put("updatedReplicas", deployment.getStatus().getUpdatedReplicas());
            }

        } catch (Exception e) {
            log.error("Failed to get deployment status: {}", e.getMessage(), e);
        }

        return status;
    }

    private void ensureNamespaceExists(KubernetesClient client, String namespace) {
        Namespace ns = client.namespaces().withName(namespace).get();
        if (ns == null) {
            client.namespaces().create(new NamespaceBuilder()
                    .withNewMetadata()
                    .withName(namespace)
                    .endMetadata()
                    .build());
            log.info("Created namespace: {}", namespace);
        }
    }

    private io.fabric8.kubernetes.api.model.apps.Deployment createKubernetesDeployment(
            Application app, String namespace) {

        Map<String, String> labels = Map.of(
                "app", app.getName(),
                "managed-by", "platform"
        );

        return new DeploymentBuilder()
                .withNewMetadata()
                    .withName(app.getName())
                    .withNamespace(namespace)
                    .withLabels(labels)
                .endMetadata()
                .withNewSpec()
                    .withReplicas(app.getReplicas())
                    .withNewSelector()
                        .withMatchLabels(labels)
                    .endSelector()
                    .withNewTemplate()
                        .withNewMetadata()
                            .withLabels(labels)
                        .endMetadata()
                        .withNewSpec()
                            .addNewContainer()
                                .withName(app.getName())
                                .withImage(app.getDockerImage())
                                .withImagePullPolicy("IfNotPresent")
                                .addAllToPorts(app.getPort() != null ?
                                    java.util.List.of(new ContainerPortBuilder()
                                        .withContainerPort(app.getPort())
                                        .withProtocol("TCP")
                                        .build()) : java.util.List.of())
                                .withEnv(createEnvVars(app))
                                .withResources(createResourceRequirements(app))
                                .withLivenessProbe(createLivenessProbe(app))
                                .withReadinessProbe(createReadinessProbe(app))
                            .endContainer()
                        .endSpec()
                    .endTemplate()
                .endSpec()
                .build();
    }

    private Service createKubernetesService(Application app, String namespace) {
        Map<String, String> labels = Map.of(
                "app", app.getName(),
                "managed-by", "platform"
        );

        return new ServiceBuilder()
                .withNewMetadata()
                    .withName(app.getName())
                    .withNamespace(namespace)
                    .withLabels(labels)
                .endMetadata()
                .withNewSpec()
                    .withSelector(labels)
                    .withType("ClusterIP")
                    .addNewPort()
                        .withName("http")
                        .withPort(app.getPort() != null ? app.getPort() : 80)
                        .withTargetPort(new IntOrString(app.getPort() != null ? app.getPort() : 80))
                        .withProtocol("TCP")
                    .endPort()
                .endSpec()
                .build();
    }

    private HorizontalPodAutoscaler createHorizontalPodAutoscaler(
            Application app, AutoScalingConfig config, String namespace) {

        return new HorizontalPodAutoscalerBuilder()
                .withNewMetadata()
                    .withName(app.getName() + "-hpa")
                    .withNamespace(namespace)
                .endMetadata()
                .withNewSpec()
                    .withNewScaleTargetRef()
                        .withApiVersion("apps/v1")
                        .withKind("Deployment")
                        .withName(app.getName())
                    .endScaleTargetRef()
                    .withMinReplicas(config.getMinReplicas())
                    .withMaxReplicas(config.getMaxReplicas())
                    .addNewMetric()
                        .withType("Resource")
                        .withNewResource()
                            .withName("cpu")
                            .withNewTarget()
                                .withType("Utilization")
                                .withAverageUtilization(config.getTargetCpuUtilization())
                            .endTarget()
                        .endResource()
                    .endMetric()
                .endSpec()
                .build();
    }

    private java.util.List<EnvVar> createEnvVars(Application app) {
        java.util.List<EnvVar> envVars = new java.util.ArrayList<>();

        if (app.getEnvironmentVariables() != null) {
            app.getEnvironmentVariables().forEach((key, value) -> {
                envVars.add(new EnvVarBuilder()
                        .withName(key)
                        .withValue(value)
                        .build());
            });
        }

        return envVars;
    }

    private ResourceRequirements createResourceRequirements(Application app) {
        Map<String, Quantity> requests = new HashMap<>();
        Map<String, Quantity> limits = new HashMap<>();

        if (app.getCpuRequest() != null) {
            requests.put("cpu", new Quantity(app.getCpuRequest()));
        }
        if (app.getMemoryRequest() != null) {
            requests.put("memory", new Quantity(app.getMemoryRequest()));
        }
        if (app.getCpuLimit() != null) {
            limits.put("cpu", new Quantity(app.getCpuLimit()));
        }
        if (app.getMemoryLimit() != null) {
            limits.put("memory", new Quantity(app.getMemoryLimit()));
        }

        return new ResourceRequirementsBuilder()
                .withRequests(requests)
                .withLimits(limits)
                .build();
    }

    private Probe createLivenessProbe(Application app) {
        if (app.getHealthCheckPath() == null) {
            return null;
        }

        return new ProbeBuilder()
                .withNewHttpGet()
                    .withPath(app.getHealthCheckPath())
                    .withPort(new IntOrString(app.getHealthCheckPort() != null ?
                            app.getHealthCheckPort() : app.getPort()))
                .endHttpGet()
                .withInitialDelaySeconds(30)
                .withPeriodSeconds(10)
                .withTimeoutSeconds(5)
                .withFailureThreshold(3)
                .build();
    }

    private Probe createReadinessProbe(Application app) {
        if (app.getHealthCheckPath() == null) {
            return null;
        }

        return new ProbeBuilder()
                .withNewHttpGet()
                    .withPath(app.getHealthCheckPath())
                    .withPort(new IntOrString(app.getHealthCheckPort() != null ?
                            app.getHealthCheckPort() : app.getPort()))
                .endHttpGet()
                .withInitialDelaySeconds(10)
                .withPeriodSeconds(5)
                .withTimeoutSeconds(3)
                .withFailureThreshold(3)
                .build();
    }
}
