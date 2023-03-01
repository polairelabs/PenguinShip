package com.navaship.api.activity;

import com.navaship.api.appuser.AppUser;
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
    private String description;
    @CreationTimestamp
    private LocalDateTime date;
    @ManyToOne
    private AppUser user;
}
