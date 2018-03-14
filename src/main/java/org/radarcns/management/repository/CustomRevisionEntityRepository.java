package org.radarcns.management.repository;

import org.radarcns.management.domain.audit.CustomRevisionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomRevisionEntityRepository extends JpaRepository<CustomRevisionEntity, Long> {
}
