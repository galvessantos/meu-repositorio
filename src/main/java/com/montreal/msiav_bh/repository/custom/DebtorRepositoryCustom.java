package com.montreal.msiav_bh.repository.custom;

import com.montreal.msiav_bh.entity.Debtor;

import java.util.List;
import java.util.Optional;

public interface DebtorRepositoryCustom {

    void saveEncrypted(Debtor debtor);

    Optional<Debtor> findDecryptedById(Long id);

    List<Debtor> findAllDecrypted();

}
