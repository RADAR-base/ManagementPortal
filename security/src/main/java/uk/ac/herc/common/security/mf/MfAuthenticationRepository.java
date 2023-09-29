package uk.ac.herc.common.security.mf;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MfAuthenticationRepository extends JpaRepository<MfAuthenticationEntity, Long>, JpaSpecificationExecutor<MfAuthenticationEntity> {
    Optional<MfAuthenticationEntity> findOneByUserName(String userName);
}
