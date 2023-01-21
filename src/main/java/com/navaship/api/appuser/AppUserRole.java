package com.navaship.api.appuser;

/**
 * UNVERIFIED_USER is a user whose email has yet to be verified
 * UNPAYED_USER is a user that hasn't paid for his subscription yet
 */
public enum AppUserRole {
    USER,
    UNVERIFIED_USER,
    UNPAYED_USER,
    ADMIN
}
