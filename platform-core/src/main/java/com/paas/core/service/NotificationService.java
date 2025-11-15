package com.paas.core.service;

import com.paas.common.enums.NotificationChannel;
import com.paas.common.enums.NotificationStatus;
import com.paas.core.entity.Notification;
import com.paas.core.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public Notification sendNotification(
            Long organizationId,
            NotificationChannel channel,
            String subject,
            String message,
            String recipient,
            Map<String, String> metadata) {

        log.info("Sending notification via {} to: {}", channel, recipient);

        Notification notification = Notification.builder()
                .organizationId(organizationId)
                .channel(channel)
                .subject(subject)
                .message(message)
                .recipient(recipient)
                .metadata(metadata != null ? metadata : Map.of())
                .status(NotificationStatus.PENDING)
                .retryCount(0)
                .build();

        notification = notificationRepository.save(notification);

        // Send async
        final Long notificationId = notification.getId();
        sendAsync(notificationId, channel, subject, message, recipient);

        return notification;
    }

    private void sendAsync(Long notificationId, NotificationChannel channel, String subject, String message, String recipient) {
        new Thread(() -> {
            try {
                updateStatus(notificationId, NotificationStatus.SENDING);

                // Simulate sending (in production, integrate with actual services)
                boolean success = sendViaChannel(channel, subject, message, recipient);

                if (success) {
                    updateStatus(notificationId, NotificationStatus.SENT);
                    log.info("Notification {} sent successfully", notificationId);
                } else {
                    handleFailure(notificationId, "Failed to send notification");
                }

            } catch (Exception e) {
                log.error("Failed to send notification: {}", e.getMessage(), e);
                handleFailure(notificationId, e.getMessage());
            }
        }).start();
    }

    private boolean sendViaChannel(NotificationChannel channel, String subject, String message, String recipient) {
        // In production, integrate with actual services
        switch (channel) {
            case EMAIL:
                return sendEmail(recipient, subject, message);
            case SLACK:
                return sendSlack(recipient, message);
            case WEBHOOK:
                return sendWebhook(recipient, subject, message);
            case SMS:
                return sendSMS(recipient, message);
            case TEAMS:
                return sendTeams(recipient, message);
            default:
                return false;
        }
    }

    private boolean sendEmail(String to, String subject, String body) {
        // TODO: Integrate with SMTP server or email service (SendGrid, AWS SES, etc.)
        log.info("Sending email to: {} - Subject: {}", to, subject);
        // Simulate success
        return true;
    }

    private boolean sendSlack(String webhookUrl, String message) {
        // TODO: Integrate with Slack webhook API
        log.info("Sending Slack message to: {}", webhookUrl);
        // Simulate success
        return true;
    }

    private boolean sendWebhook(String url, String subject, String body) {
        // TODO: Send HTTP POST request to webhook URL
        log.info("Sending webhook to: {}", url);
        // Simulate success
        return true;
    }

    private boolean sendSMS(String phoneNumber, String message) {
        // TODO: Integrate with Twilio, AWS SNS, etc.
        log.info("Sending SMS to: {}", phoneNumber);
        // Simulate success
        return true;
    }

    private boolean sendTeams(String webhookUrl, String message) {
        // TODO: Integrate with Microsoft Teams webhook API
        log.info("Sending Teams message to: {}", webhookUrl);
        // Simulate success
        return true;
    }

    @Transactional
    private void updateStatus(Long notificationId, NotificationStatus status) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setStatus(status);
            if (status == NotificationStatus.SENT) {
                notification.setSentAt(LocalDateTime.now());
            }
            notificationRepository.save(notification);
        });
    }

    @Transactional
    private void handleFailure(Long notificationId, String errorMessage) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setStatus(NotificationStatus.FAILED);
            notification.setErrorMessage(errorMessage);
            notification.setRetryCount(notification.getRetryCount() + 1);
            notificationRepository.save(notification);
        });
    }

    @Transactional(readOnly = true)
    public List<Notification> getNotificationsByOrganization(Long organizationId) {
        return notificationRepository.findByOrganizationId(organizationId);
    }

    @Transactional(readOnly = true)
    public List<Notification> getNotificationsByStatus(NotificationStatus status) {
        return notificationRepository.findByStatus(status);
    }
}
