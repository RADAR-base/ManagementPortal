package org.radarbase.management.service.dto;

import org.radarbase.management.domain.enumeration.DataGroupingType;

import javax.persistence.Column;
import java.time.Instant;

public class DataLogDTO {
    private DataGroupingType groupingType;

    private Instant time;

    public DataGroupingType getGroupingType() {
        return groupingType;
    }

    public void setGroupingType(DataGroupingType groupingType) {
        this.groupingType = groupingType;
    }

    public Instant getTime() {
        return time;
    }

    public void setTime(Instant time) {
        this.time = time;
    }

}
