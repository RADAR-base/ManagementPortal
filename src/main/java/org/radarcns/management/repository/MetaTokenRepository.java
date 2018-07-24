package org.radarcns.management.repository;

import org.radarcns.management.domain.MetaToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.history.RevisionRepository;

public interface MetaTokenRepository extends JpaRepository<MetaToken, Long>,
    RevisionRepository<MetaToken, Long, Integer> {
}
