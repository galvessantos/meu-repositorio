package com.montreal.msiav_bh.repository.impl;

import com.montreal.msiav_bh.entity.Address;
import com.montreal.msiav_bh.entity.Debtor;
import com.montreal.msiav_bh.repository.custom.DebtorRepositoryCustom;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DebtorRepositoryImpl implements DebtorRepositoryCustom {

    private final JdbcTemplate jdbc;

    @Override
    public void saveEncrypted(Debtor debtor) {
        String sql = """
            INSERT INTO debtor (name, cpf_cnpj, email, cell_phone, address_id, created_at)
            VALUES (
                criptografar(?::text), 
                criptografar(?::text), 
                criptografar(?::text), 
                criptografar(?::text), 
                ?, ?
            )
        """;

        jdbc.update(sql,
                debtor.getName(),
                debtor.getCpfCnpj(),
                debtor.getEmail(),
                debtor.getCellPhone(),
                debtor.getAddress().getId(),
                Timestamp.valueOf(LocalDateTime.now())
        );
    }

    @Override
    public Optional<Debtor> findDecryptedById(Long id) {
        String sql = """
            SELECT id,
                   descriptografar(name) AS name,
                   descriptografar(cpf_cnpj) AS cpf_cnpj,
                   descriptografar(email) AS email,
                   descriptografar(cell_phone) AS cell_phone,
                   address_id,
                   created_at
            FROM debtor
            WHERE id = ?
        """;

        return jdbc.query(sql, rs -> {
            if (rs.next()) {
                Debtor debtor = new Debtor();
                debtor.setId(rs.getLong("id"));
                debtor.setName(rs.getString("name"));
                debtor.setCpfCnpj(rs.getString("cpf_cnpj"));
                debtor.setEmail(rs.getString("email"));
                debtor.setCellPhone(rs.getString("cell_phone"));
                debtor.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());

                Address address = new Address();
                address.setId(rs.getLong("address_id"));
                debtor.setAddress(address);

                return Optional.of(debtor);
            }
            return Optional.empty();
        }, id);
    }

    @Override
    public List<Debtor> findAllDecrypted() {
        String sql = """
            SELECT id,
                   descriptografar(name) AS name,
                   descriptografar(cpf_cnpj) AS cpf_cnpj,
                   descriptografar(email) AS email,
                   descriptografar(cell_phone) AS cell_phone,
                   address_id,
                   created_at
            FROM debtor
        """;

        return jdbc.query(sql, (rs, rowNum) -> {
            Debtor debtor = new Debtor();
            debtor.setId(rs.getLong("id"));
            debtor.setName(rs.getString("name"));
            debtor.setCpfCnpj(rs.getString("cpf_cnpj"));
            debtor.setEmail(rs.getString("email"));
            debtor.setCellPhone(rs.getString("cell_phone"));
            debtor.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());

            Address address = new Address();
            address.setId(rs.getLong("address_id"));
            debtor.setAddress(address);

            return debtor;
        });
    }
}
