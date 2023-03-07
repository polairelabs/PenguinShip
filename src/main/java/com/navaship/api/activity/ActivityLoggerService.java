package com.navaship.api.activity;

import com.navaship.api.appuser.AppUser;
import com.navaship.api.shipment.Shipment;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.util.List;

@Service
@AllArgsConstructor
public class ActivityLoggerService {
    public static final String SHIPMENT_CREATED = "Shipment #%d was created";
    public static final String SHIPMENT_CREATED_SUB = "Shipment #%d will be delivered to %s";
    public static final String SHIPMENT_BOUGHT = "Shipment #%d was purchased for $%s";
    public static final String SHIPMENT_STATUS_CHANGE = "Shipment #%d status has changed to %s";
    // public static final String SHIPMENT_RETURNED = "Shipment #%d is being returned";

    private ActivityLogRepository activityLogRepository;
    private ModelMapper modelMapper;


    public String getShipmentCreatedMessage(Shipment shipment) {
        return String.format(SHIPMENT_CREATED, shipment.getId());
    }

    public String getShipmentBoughtMessage(Shipment shipment) {
        return String.format(SHIPMENT_BOUGHT, shipment.getId(), shipment.getRate().getRate());
    }

    public String getShipmentStatusChangeMessage(Shipment shipment) {
        return String.format(SHIPMENT_STATUS_CHANGE, shipment.getId(), shipment.getEasyPostStatus());
    }

    public ActivityLog insert(AppUser user, Shipment shipment, String message, ActivityMessageType messageType) {
        ActivityLog activityLog = new ActivityLog();
        activityLog.setUser(user);
        activityLog.setShipment(shipment);
        activityLog.setMessage(message);
        activityLog.setMessageType(messageType);
        return activityLogRepository.save(activityLog);
    }

    public List<ActivityLog> findLatestActivityLogs(AppUser user) {
        return activityLogRepository.findTop10ByUserOrderByCreatedAtDesc(user);
    }

    public List<ActivityLogResponse> convertToActivityLogResponse(List<ActivityLog> activityLogs) {
        Type listType = new TypeToken<List<ActivityLogResponse>>() {}.getType();
        return modelMapper.map(activityLogs, listType);
    }
}
