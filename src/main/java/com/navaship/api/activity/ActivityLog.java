package com.navaship.api.activity;

import com.navaship.api.appuser.AppUser;
import com.navaship.api.shipment.Shipment;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity to handle all user logging when user interacts with the API in certain scenarios e.g. purchasing a shipment, etc.
 */
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@Entity
public class ActivityLog {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String message;
    private String subMessage;
    @Enumerated(EnumType.STRING)
    private ActivityMessageType messageType = ActivityMessageType.NONE;
    @CreationTimestamp
    private LocalDateTime createdAt;
    @ManyToOne
    private AppUser user;
    @ManyToOne
    private Shipment shipment;
}
