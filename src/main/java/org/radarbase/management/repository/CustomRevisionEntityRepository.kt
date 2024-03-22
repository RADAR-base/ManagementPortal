package org.radarbase.management.repository

import org.radarbase.management.domain.audit.CustomRevisionEntity
import org.springframework.data.jpa.repository.JpaRepository

interface CustomRevisionEntityRepository : JpaRepository<CustomRevisionEntity?, Int?>
