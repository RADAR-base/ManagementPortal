package org.radarbase.management.repository;

import org.radarbase.management.domain.audit.CustomRevisionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomRevisionEntityRepository extends JpaRepository<CustomRevisionEntity,
        Integer> {
}
