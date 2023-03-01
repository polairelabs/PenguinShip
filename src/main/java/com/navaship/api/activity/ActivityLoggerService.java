package com.navaship.api.activity;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ActivityLoggerService {
    private ActivityLogRepository activityLogRepository;


    public ActivityLog insertShipmentTimeLineActivity() {
        return activityLogRepository.save(null);
    }
}
