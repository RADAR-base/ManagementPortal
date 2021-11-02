package org.radarbase.management.web.rest;

import org.radarbase.management.config.DefaultProfileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Resource to return information about the currently running Spring profiles.
 */
@RestController
@RequestMapping("/api")
public class ProfileInfoResource {

    @Autowired
    private Environment env;

    @GetMapping("/profile-info")
    public ProfileInfoVM getActiveProfiles() {
        String[] activeProfiles = DefaultProfileUtil.getActiveProfiles(env);
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
            return activeProfiles;
        }
    }
}
