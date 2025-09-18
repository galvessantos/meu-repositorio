package com.montreal.msiav_bh.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.montreal.msiav_bh.entity.Debtor;
import com.montreal.msiav_bh.repository.custom.DebtorRepositoryCustom;

import java.util.Optional;

@Repository
public interface DebtorRepository extends JpaRepository<Debtor, Long>, DebtorRepositoryCustom {

    @Query(value = """
        SELECT * FROM debtor d 
        WHERE 
            (:fieldValue IS NULL OR 
            CASE 
                WHEN :fieldName = 'name' THEN descriptografar(d.name)
                WHEN :fieldName = 'cpfCnpj' THEN descriptografar(d.cpf_cnpj)
                ELSE NULL 
            END ILIKE CONCAT('%', :fieldValue, '%'))
        """,
        nativeQuery = true)
    Page<Debtor> searchByDynamicField(
            @Param("fieldName") String fieldName,
            @Param("fieldValue") String fieldValue,
            Pageable pageable);

    Optional<Debtor> findByCpfCnpj(String s);
}
