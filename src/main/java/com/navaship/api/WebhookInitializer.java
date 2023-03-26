package com.navaship.api;

import com.easypost.exception.EasyPostException;
import com.easypost.model.Webhook;
import com.navaship.api.easypost.EasyPostService;
import com.navaship.api.webhook.WebhookService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@RequiredArgsConstructor
public class WebhookInitializer {
    enum WebhookType {
        EASYPOST
    }

    private final EasyPostService easyPostService;
    private final WebhookService webhookService;

    @PostConstruct
    public void createWebhook() {
//        try {
//            Webhook webhook = easyPostService.createWebhook();
//            com.navaship.api.webhook.Webhook easypostWebhook = new com.navaship.api.webhook.Webhook();
//            easypostWebhook.setWebhookId(webhook.getId());
//            easypostWebhook.setType(WebhookType.EASYPOST.name());
//            webhookService.createWebhook(easypostWebhook);
//        } catch (EasyPostException e) {
//            throw new RuntimeException(e);
//        }
    }
}
