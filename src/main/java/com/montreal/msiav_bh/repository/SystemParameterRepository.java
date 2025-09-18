package com.montreal.msiav_bh.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.montreal.msiav_bh.entity.SystemParameter;
import com.montreal.oauth.domain.repository.infrastructure.CustomJpaRepository;

@Repository
public interface SystemParameterRepository extends CustomJpaRepository<SystemParameter, Long> {

    @Query("SELECT s FROM SystemParameter s WHERE s.system = :system AND s.parameter = :parameter")
    Optional<SystemParameter> findBySystemAndParameter(@Param("system") String system, @Param("parameter") String parameter);
}
