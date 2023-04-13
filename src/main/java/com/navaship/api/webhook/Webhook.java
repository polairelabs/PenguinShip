package com.navaship.api.webhook;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@Entity
public class Webhook {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String webhookId;
    // Full url
    private String url;
    @Column(unique = true)
    @Enumerated(EnumType.STRING)
    private WebhookType type;
}
