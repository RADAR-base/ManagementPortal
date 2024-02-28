package org.radarbase.management.service.dto;

import org.radarbase.management.domain.enumeration.DataGroupingType;
import com.fasterxml.jackson.annotation.JsonInclude

import javax.persistence.Column;
import java.time.Instant;




@JsonInclude(JsonInclude.Include.NON_NULL)
class DataLogDTO {
    var groupingType: DataGroupingType? = null;
    var time: Instant? = null;

    override fun toString(): String {
        return ("DataLogDTO{" + "groupingType=" + groupingType
                + ", time=" + time + '\''
                + '}')
    }




}
