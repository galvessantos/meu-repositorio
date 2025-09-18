package com.montreal.oauth.domain.repository;

import java.util.List;
import java.util.Optional;

import com.montreal.oauth.domain.dto.request.RoleFunctionalitiesRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.montreal.oauth.domain.entity.Functionality;
import com.montreal.oauth.domain.entity.Role;
import com.montreal.oauth.domain.entity.RoleFunctionality;

@Repository
public interface RoleFunctionalityRepository extends JpaRepository<RoleFunctionality, Long> {
	
    List<RoleFunctionality> findByRoleId(Long roleId);
    
    boolean existsByRoleAndFunctionality(Role role, Functionality functionality);

    void deleteByRoleAndFunctionality(Role role, Functionality functionality);
    
    Optional<RoleFunctionality> findByRoleAndFunctionality(Role role, Functionality functionality);

    void deleteByFunctionalityId(Long id);

    List<RoleFunctionalitiesRequest> findByFunctionalityId(Long id);

    void deleteByFunctionalityIdAndRoleIdIn(@Param("functionalityId") Long functionalityId, @Param("roleIds") List<Integer> roleIds);

    Optional<RoleFunctionality> findByRoleIdAndFunctionalityId(Long roleId, Long id);

    void deleteByRoleId(Integer roleId);
}
