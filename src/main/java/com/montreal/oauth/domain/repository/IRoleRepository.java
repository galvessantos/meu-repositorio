package com.montreal.oauth.domain.repository;

import com.montreal.oauth.domain.entity.Role;
import com.montreal.oauth.domain.enumerations.RoleEnum;
import com.montreal.oauth.domain.repository.infrastructure.CustomJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IRoleRepository extends CustomJpaRepository<Role, Long> {

    Optional<Role> findByName(RoleEnum name);

}
