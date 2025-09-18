package com.montreal.oauth.domain.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.montreal.oauth.domain.entity.RoleFunctionality;
import com.montreal.oauth.domain.repository.RoleFunctionalityRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoleFunctionalityService {

    private final RoleFunctionalityRepository roleFunctionalityRepository;


    public List<RoleFunctionality> findByRoleId(Long roleId) {
        return roleFunctionalityRepository.findByRoleId(roleId);
    }

    public RoleFunctionality create(RoleFunctionality roleFunctionality) {
        return roleFunctionalityRepository.save(roleFunctionality);
    }

    public void deleteById(Long id) {
        roleFunctionalityRepository.deleteById(id);
    }

    public Optional<RoleFunctionality> findById(Long id) {return roleFunctionalityRepository.findById(id);}


}

