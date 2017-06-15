package org.radarcns.management.service.dto;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by nivethika on 13-6-17.
 */
public class PatientDeviceAssignmentDTO implements Serializable {

    private Long id;

    private String login;

    private String projectName;

    private Set<DescriptiveDeviceDTO> devices = new HashSet<>();
}
