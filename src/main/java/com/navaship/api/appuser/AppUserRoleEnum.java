package com.navaship.api.appuser;

/**
 * ADMIN is the boss
 * USER has a valid subscription
 * UNPAID_USER WAS a USER who had a subscription, but he didn't renew his subscription, he can only read past data
 * NEW_USER is a user who has yet to pay for a subscription, but used his email for signup
 */
public enum AppUserRoleEnum {
    ADMIN,
    USER,
    UNPAID_USER,
    NEW_USER // TODO check if it works with new registration
}
