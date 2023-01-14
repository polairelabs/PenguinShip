package com.navaship.api.shipments;

/**
 * <a href="https://support.easypost.com/hc/en-us/articles/360044353091-Tracking-Frequently-Asked-Questions#h_9656edcc-0b76-4851-8b09-a826d9add238">Click here!!</a>
 * Status from easypost: unknown, pre_transit, in_transit, out_for_delivery, available_for_pickup, delivered, return_to_sender, failure, canceled, error
 */
public enum EasyPostShipmentStatus {
    UNKNOWN,
    PRE_TRANSIT,
    IN_TRANSIT,
    OUT_FOR_DELIVERY,
    AVAILABLE_FOR_PICKUP,
    DELIVERED,
    RETURN_TO_SENDER,
    FAILURE,
    CANCELED,
    ERROR
}
