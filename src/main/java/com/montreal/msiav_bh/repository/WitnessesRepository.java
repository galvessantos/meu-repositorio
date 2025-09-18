package com.montreal.msiav_bh.repository;

import com.montreal.msiav_bh.entity.Witnesses;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WitnessesRepository extends JpaRepository<Witnesses, Long> {
}
