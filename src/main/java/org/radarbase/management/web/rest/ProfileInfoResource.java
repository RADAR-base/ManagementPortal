package org.radarbase.management.web.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

/**
 * Resource to return information about the currently running Spring profiles.
 */
@RestController
@RequestMapping("/api")
public class ProfileInfoResource {

    @Autowired
    private Environment env;

    /**
     * Get profile info.
     * @return profile info.
     */
    @GetMapping("/profile-info")
    public ProfileInfoVM getActiveProfiles() {
        String[] activeProfiles = env.getActiveProfiles();
        if (activeProfiles.length == 0) {
            activeProfiles = env.getDefaultProfiles();
        }
        return new ProfileInfoVM(activeProfiles);
    }

    static class ProfileInfoVM {
        private final String[] activeProfiles;

        ProfileInfoVM(String[] activeProfiles) {
            this.activeProfiles = new String[activeProfiles.length];
            System.arraycopy(activeProfiles, 0,
                    this.activeProfiles, 0, activeProfiles.length);
        }

        public String[] getActiveProfiles() {
            return Arrays.copyOf(activeProfiles, activeProfiles.length);
        }
    }
}
