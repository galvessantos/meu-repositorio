package com.montreal.oauth.domain.repository;

import com.montreal.oauth.domain.entity.Role;
import com.montreal.oauth.domain.enumerations.RoleEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {

    Optional<Role> findByName(RoleEnum name);

}
