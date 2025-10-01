package com.montreal.oauth.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.montreal.oauth.domain.enumerations.RoleEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@Entity
@Table(name = "roles")
@AllArgsConstructor
@NoArgsConstructor
public class Role implements Serializable {
    @Serial
    private static final long serialVersionUID = -7637338037580225606L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private RoleEnum name;

    @Column(name = "is_requires_token_first_login")
    private Boolean requiresTokenFirstLogin;

    @Column(name = "is_biometric_validation")
    private Boolean biometricValidation;

    @Column(name = "is_token_login")
    private Boolean tokenLogin;

    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<RolePermission> rolePermissions = new HashSet<>();

    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<RoleFunctionality> roleFunctionalities = new HashSet<>();

    @JsonProperty("functionalityIds")
    public List<Long> getFunctionalityIds() {
        if (roleFunctionalities == null || roleFunctionalities.isEmpty()) {
            return new ArrayList<>();
        }

        // Force o carregamento se for Lazy
        Hibernate.initialize(roleFunctionalities);

        return roleFunctionalities.stream()
                .map(rf -> rf.getFunctionality().getId())
                .collect(Collectors.toList());
    }

    @JsonProperty("functionalities")
    public List<Functionality> getFunctionalities() {
        if (roleFunctionalities == null || roleFunctionalities.isEmpty()) {
            return new ArrayList<>();
        }

        // Force o carregamento se for Lazy
        Hibernate.initialize(roleFunctionalities);

        return roleFunctionalities.stream()
                .map(RoleFunctionality::getFunctionality)
                .collect(Collectors.toList());
    }

    public Role(String name) {
        this.name = RoleEnum.valueOf(name);
    }
}
