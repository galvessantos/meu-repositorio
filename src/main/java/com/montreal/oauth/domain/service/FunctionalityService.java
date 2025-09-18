package com.montreal.oauth.domain.service;

import com.montreal.oauth.domain.dto.FunctionalityDTO;
import com.montreal.oauth.domain.dto.response.FunctionalityResponseDTO;
import com.montreal.oauth.domain.entity.Functionality;
import com.montreal.oauth.domain.exception.DuplicateResourceException;
import com.montreal.oauth.domain.exception.ResourceNotFoundException;
import com.montreal.oauth.domain.mapper.FunctionalityMapper;
import com.montreal.oauth.domain.repository.FunctionalityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FunctionalityService {

    public static final String MESSAGE_FUNCTIONALITY_NOT_FOUND = "Funcionalidade não encontrada com ID: ";
    private final FunctionalityRepository functionalityRepository;
    private final FunctionalityMapper functionalityMapper;

    public List<Functionality> findAll() {
        return functionalityRepository.findAll();
    }

    public FunctionalityResponseDTO findByName(String name) {
        Functionality functionality = functionalityRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Funcionalidade não encontrada: " + name));
        return functionalityMapper.toResponseDTO(functionality);
    }

    public Functionality create(FunctionalityDTO dto) {
        log.info("Verificando se a funcionalidade '{}' já existe...", dto.getName());

        Optional<Functionality> existingFunctionality = functionalityRepository.findById(dto.getId());
        if (existingFunctionality.isPresent()) {
            throw new DuplicateResourceException("Já existe uma funcionalidade com o nome '" + dto.getName() + "'.");
        }

        Functionality functionality = functionalityMapper.toEntity(dto);
        return functionalityRepository.save(functionality);
    }

    public Functionality update(Long id, FunctionalityDTO dto) {
        Functionality functionality = functionalityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MESSAGE_FUNCTIONALITY_NOT_FOUND + id));


        Optional<Functionality> existingFunctionality = functionalityRepository.findByName(dto.getName());
        if (existingFunctionality.isPresent() && !existingFunctionality.get().getId().equals(id)) {
            throw new DuplicateResourceException("Já existe uma funcionalidade com o nome '" + dto.getName() + "'.");
        }

        functionality.setName(dto.getName());
        functionality.setDescription(dto.getDescription());
        return functionalityRepository.save(functionality);
    }

    public void deleteById(Long id) {
        Functionality functionality = functionalityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MESSAGE_FUNCTIONALITY_NOT_FOUND + id));

        functionalityRepository.delete(functionality);
    }
}
