package org.radarbase.management.domain;


import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.envers.Audited;
import org.radarbase.management.domain.enumeration.DataGroupingType;
import org.radarbase.management.domain.support.AbstractEntityListener;

import javax.persistence.*;
import java.time.Instant;



@Entity
@Audited
@Table(name = "connect_data_log")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@EntityListeners({AbstractEntityListener.class})
public class ConnectDataLog {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator", initialValue = 1000,
            sequenceName = "hibernate_sequence")
    private Long id;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "data_grouping_type")
    @Enumerated(EnumType.STRING)
    private DataGroupingType dataGroupingType;


    @Column(name = "time")
    private Instant time;


    public Instant getTime() {
        return time;
    }

    public void setTime(Instant time) {
        this.time = time;
    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DataGroupingType getDataGroupingType() {
        return dataGroupingType;
    }

    public void setDataGroupingType(DataGroupingType dataGroupingType) {
        this.dataGroupingType = dataGroupingType;
    }
}

