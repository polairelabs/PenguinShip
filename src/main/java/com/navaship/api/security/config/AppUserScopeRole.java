package com.navaship.api.security.config;

/**
 * The default JwtGrantedAuthoritiesConverter implementation reads the authorities from the scope claim (which you provided in the JWT) and prefixes them with SCOPE_
 * This enum is used in place of AppUserRole Enum in SecurityConfig FilterChain
 */
public enum AppUserScopeRole {
    SCOPE_ROLE_ADMIN,
    SCOPE_ROLE_USER,
    SCOPE_ROLE_UNPAID_USER
}
