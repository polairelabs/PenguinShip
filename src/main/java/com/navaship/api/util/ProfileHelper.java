package com.navaship.api.util;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class ProfileHelper {
    private static final String DEV_PROFILE = "dev";
    private static final String PROD_PROFILE = "prod";

    private Environment env;

    public boolean isDevProfileActive() {
        String[] activeProfiles = env.getActiveProfiles();
        for (String profile : activeProfiles) {
            if (DEV_PROFILE.equalsIgnoreCase(profile)) {
                return true;
            }
        }
        return false;
    }

    public boolean isProdProfileActive() {
        String[] activeProfiles = env.getActiveProfiles();
        for (String profile : activeProfiles) {
            if (PROD_PROFILE.equalsIgnoreCase(profile)) {
                return true;
            }
        }
        return false;
    }
}
