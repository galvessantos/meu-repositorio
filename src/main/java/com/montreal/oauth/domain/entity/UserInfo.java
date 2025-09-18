package com.montreal.oauth.domain.entity;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import com.montreal.core.utils.CryptoUtil;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Data
@ToString
@AllArgsConstructor
@Table(name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "username")

        })
@NoArgsConstructor
public class UserInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(max = 20)
    @Column(name = "username")
    private String username;

    @Size(max = 50)
    @Column(name = "email")
    private String email;

    @Size(max = 120)
    @Column(name = "password")
    private String password;

    @Size(max = 120)
    @Column(name = "fullname")
    private String fullName;

    @Size(max = 300)
    @Column(name = "cpf", nullable = false , unique = true)
    private String cpf;

    @Size(max = 300)
    @Column(name = "phone")
    private String phone;

    @Column(name = "company_id")
    private String companyId;

    @Size(max = 1000)
    @Column(name = "link")
    private String link;

    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Role> roles = new HashSet<>();

    @Column(name = "token_temporary")
    private String tokenTemporary;

    @Column(name = "token_expired_at")
    private LocalDateTime tokenExpiredAt;

    private boolean isReset;
    private Timestamp resetAt;
    private boolean isEnabled;
    private boolean isCreatedByAdmin;
    private boolean isPasswordChangedByUser;

    public boolean isCreatedByAdmin() {
        return this.isCreatedByAdmin;
    }

    public void setCreatedByAdmin(boolean isCreatedByAdmin) {
        this.isCreatedByAdmin = isCreatedByAdmin;
    }

    public boolean isPasswordChangedByUser() {
        return this.isPasswordChangedByUser;
    }

    public void setPasswordChangedByUser(boolean isPasswordChangedByUser) {
        this.isPasswordChangedByUser = isPasswordChangedByUser;
    }
    
    public void encryptFields(CryptoUtil crypto) {
        if (fullName != null) fullName = crypto.encrypt(fullName);
        if (cpf != null) cpf = crypto.encrypt(cpf);
        if (phone != null) phone = crypto.encrypt(phone);
    }

    public void decryptFields(CryptoUtil crypto) {
        if (fullName != null) fullName = crypto.decrypt(fullName);
        if (cpf != null) cpf = crypto.decrypt(cpf);
        if (phone != null) phone = crypto.decrypt(phone);
    }

}
