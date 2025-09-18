package com.montreal.oauth.domain.repository;

import com.montreal.oauth.domain.entity.Functionality;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface FunctionalityRepository extends JpaRepository<Functionality, Long> {

    List<Functionality> findByParentIdIsNull();

    Optional<Functionality> findByName(String name);

    @Query("SELECT f.id FROM Role r " +
            "JOIN r.roleFunctionalities rf " +
            "JOIN rf.functionality f " +
            "WHERE r.id = :roleId")
    Set<Long> findFunctionalityIdsByRoleId(Long roleId);
    List<Functionality> findByOrderByPositionIndexAsc();


}


