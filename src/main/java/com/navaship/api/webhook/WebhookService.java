package com.navaship.api.webhook;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class WebhookService {
    private WebhookRepository webhookRepository;

    public void createWebhook(Webhook webhook) {
        webhookRepository.save(webhook);
    }

    public Webhook retrieveWebhookWithType(String type) {
        return webhookRepository.findWebhookByType(type);
    }
}
