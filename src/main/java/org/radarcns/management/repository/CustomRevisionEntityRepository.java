package org.radarcns.management.repository;

import org.radarcns.management.domain.audit.CustomRevisionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;

public interface CustomRevisionEntityRepository extends 
                    JpaRepository<CustomRevisionEntity, Integer> {

    Page<CustomRevisionEntity> findAllByTimestampBetween(Date fromDate, Date toDate, 
                                Pageable pageable);
}
