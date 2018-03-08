package org.radarcns.management.service.dto;

import java.util.List;

public class ProjectSubjectsDTO {

    private List<SubjectDTO> activeParticipants;

    private List<SubjectDTO> inactiveParticipants;

    public List<SubjectDTO> getActiveParticipants() {
        return activeParticipants;
    }

    public void setActiveParticipants(
            List<SubjectDTO> activeParticipants) {
        this.activeParticipants = activeParticipants;
    }

    public List<SubjectDTO> getInactiveParticipants() {
        return inactiveParticipants;
    }

    public void setInactiveParticipants(
            List<SubjectDTO> inactiveParticipants) {
        this.inactiveParticipants = inactiveParticipants;
    }
}
