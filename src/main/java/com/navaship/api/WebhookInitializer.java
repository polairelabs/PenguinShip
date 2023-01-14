package com.navaship.api;

import com.easypost.exception.EasyPostException;
import com.easypost.model.Webhook;
import com.navaship.api.easypost.EasyPostService;
import com.navaship.api.webhook.WebhookService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@RequiredArgsConstructor
public class WebhookInitializer {
    private final EasyPostService easyPostService;
    private final WebhookService webhookService;
    @Value("${navaship.app.easypost.create.webhook}")
    private boolean shouldCreateEasyPostWebhook;

    @PostConstruct
    public void createWebhook() {
        try {
            if (!shouldCreateEasyPostWebhook) {
                return;
            }
            Webhook webhook = easyPostService.createWebhook();
            com.navaship.api.webhook.Webhook easypostWebhook = new com.navaship.api.webhook.Webhook();
            easypostWebhook.setWebhookId(webhook.getId());
            easypostWebhook.setType("EASYPOST");
            webhookService.createWebhook(easypostWebhook);
        } catch (EasyPostException e) {
            throw new RuntimeException(e);
        }
    }
}
