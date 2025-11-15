package com.paas.api.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class LogStreamingController {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Send application log message to subscribers
     */
    public void sendApplicationLog(Long applicationId, String logLine) {
        String destination = "/topic/logs/app/" + applicationId;
        messagingTemplate.convertAndSend(destination, logLine);
        log.debug("Sent log to {}: {}", destination, logLine);
    }

    /**
     * Send build log message to subscribers
     */
    public void sendBuildLog(Long buildId, String logLine) {
        String destination = "/topic/builds/" + buildId;
        messagingTemplate.convertAndSend(destination, logLine);
        log.debug("Sent build log to {}: {}", destination, logLine);
    }

    /**
     * Send deployment update to subscribers
     */
    public void sendDeploymentUpdate(Long deploymentId, Object update) {
        String destination = "/topic/deployments/" + deploymentId;
        messagingTemplate.convertAndSend(destination, update);
        log.debug("Sent deployment update to {}", destination);
    }

    /**
     * Subscribe to application logs
     */
    @MessageMapping("/logs/subscribe/{applicationId}")
    public void subscribeToLogs(@DestinationVariable Long applicationId) {
        log.info("Client subscribed to logs for application: {}", applicationId);
        // Kubernetes pod log tailing would be implemented here
        // For now, send a welcome message
        sendApplicationLog(applicationId, "Connected to log stream for application " + applicationId);
    }

    /**
     * Subscribe to build logs
     */
    @MessageMapping("/builds/subscribe/{buildId}")
    public void subscribeToBuildLogs(@DestinationVariable Long buildId) {
        log.info("Client subscribed to build logs for build: {}", buildId);
        sendBuildLog(buildId, "Connected to build log stream for build " + buildId);
    }

    /**
     * Subscribe to deployment updates
     */
    @MessageMapping("/deployments/subscribe/{deploymentId}")
    public void subscribeToDeploymentUpdates(@DestinationVariable Long deploymentId) {
        log.info("Client subscribed to deployment updates for deployment: {}", deploymentId);
        sendDeploymentUpdate(deploymentId, "Connected to deployment update stream for deployment " + deploymentId);
    }
}
