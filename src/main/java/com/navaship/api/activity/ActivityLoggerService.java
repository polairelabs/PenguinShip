package com.navaship.api.activity;

import com.navaship.api.appuser.AppUser;
import com.navaship.api.easypost.EasypostShipmentStatus;
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
    private static final String SHIPMENT_CREATED = "Shipment #%d was created";
    // private static final String SHIPMENT_CREATED_SUB = "Shipment #%d will be delivered to %s";
    private static final String SHIPMENT_BOUGHT = "Shipment #%d was purchased";
    private static final String SHIPMENT_STATUS_CHANGE = "Shipment #%d status has been updated to %s";
    public static final String SHIPMENT_RETURN_STARTED = "Shipment #%d is being cancelled";
    public static final String SHIPMENT_RETURN_PROCESSED = "Shipment #%d has been cancelled. A refund of $%.2f has been issued.";

    private ActivityLogRepository activityLogRepository;
    private ModelMapper modelMapper;


    public ActivityLog insert(AppUser user, Shipment shipment, String message, ActivityMessageType messageType) {
        ActivityLog activityLog = new ActivityLog();
        activityLog.setUser(user);
        activityLog.setShipment(shipment);
        activityLog.setMessage(message);
        activityLog.setMessageType(messageType);
        if (shipment.getEasypostStatus() == null) {
            activityLog.setEasypostStatus(EasypostShipmentStatus.NONE);
        } else {
            activityLog.setEasypostStatus(shipment.getEasypostStatus());
        }
        return activityLogRepository.save(activityLog);
    }

    public String getShipmentCreatedMessage(Shipment shipment) {
        return String.format(SHIPMENT_CREATED, shipment.getShipmentNumber());
    }

    public String getShipmentBoughtMessage(Shipment shipment) {
        return String.format(SHIPMENT_BOUGHT, shipment.getShipmentNumber(), shipment.getRate().getRate());
    }

    public String getShipmentStatusChangeMessage(Shipment shipment) {
        return String.format(SHIPMENT_STATUS_CHANGE, shipment.getShipmentNumber(), convertToTitleCase(shipment.getEasypostStatus().toString()));
    }

    public String getShipmentReturnStarted(Shipment shipment) {
        return String.format(SHIPMENT_RETURN_STARTED, shipment.getShipmentNumber());
    }

    public String getShipmentReturnProcessed(Shipment shipment, double refundAmount) {
        return String.format(SHIPMENT_RETURN_PROCESSED, shipment.getShipmentNumber(), refundAmount);
    }

    public List<ActivityLog> findLatestActivityLogs(AppUser user) {
        return activityLogRepository.findTop10ByUserOrderByCreatedAtDesc(user);
    }

    public List<ActivityLogResponse> convertToActivityLogResponse(List<ActivityLog> activityLogs) {
        Type listType = new TypeToken<List<ActivityLogResponse>>() {}.getType();
        return modelMapper.map(activityLogs, listType);
    }

    public String convertToTitleCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        String[] words = input.split("_");
        StringBuilder titleCase = new StringBuilder();

        for (String word : words) {
            if (word.length() > 0) {
                titleCase.append(Character.toUpperCase(word.charAt(0)));
                titleCase.append(word.substring(1).toLowerCase());
                titleCase.append(" ");
            }
        }

        return titleCase.toString().trim();
    }

}
