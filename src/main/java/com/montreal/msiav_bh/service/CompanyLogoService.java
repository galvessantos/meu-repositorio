package com.montreal.msiav_bh.service;

import com.montreal.core.domain.exception.NotFoundException;
import com.montreal.msiav_bh.dto.CompanyLogoFileDTO;
import com.montreal.msiav_bh.dto.response.CompanyLogoResponse;
import com.montreal.msiav_bh.entity.Company;
import com.montreal.msiav_bh.entity.CompanyLogo;
import com.montreal.msiav_bh.repository.CompanyLogoRepository;
import com.montreal.msiav_bh.repository.CompanyRepository;
import com.montreal.oauth.exception.BusinessException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CompanyLogoService {

    private final CompanyLogoRepository companyLogoRepository;
    private final CompanyRepository companyRepository;

    public CompanyLogoResponse uploadCompanyLogo(Long companyId, String base64Logo) {
        validateCompanyExists(companyId);

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new NotFoundException("Empresa não encontrada para o ID: " + companyId));

        CompanyLogo companyLogo = companyLogoRepository.findByCompany(company)
                .orElseGet(() -> new CompanyLogo(company));

        companyLogo.setCompanyImage(base64Logo);

        CompanyLogo saved = companyLogoRepository.save(companyLogo);

        return CompanyLogoResponse.builder()
                .message("Logo salva com sucesso.")
                .companyId(saved.getCompany().getId())
                .build();
    }


    @Transactional
    public CompanyLogoResponse updateCompanyLogo(Long companyId, String base64Logo) {
        validateCompanyExists(companyId);

        CompanyLogo logo = findLogoOrThrow(companyId);
        logo.setCompanyImage(base64Logo);
        CompanyLogo updated = companyLogoRepository.save(logo);

        return CompanyLogoResponse.builder()
                .message("Logo atualizado com sucesso.")
                .companyId(updated.getCompany().getId())
                .build();
    }


    public CompanyLogoFileDTO getCompanyLogoBase64(Long companyId) {
        CompanyLogo logo = companyLogoRepository.findByCompanyId(companyId)
                .orElseThrow(() -> new NotFoundException("Logo não encontrada para a empresa de ID: " + companyId));

        return new CompanyLogoFileDTO(logo.getCompanyImage());
    }

    @Transactional
    public void deleteCompanyLogo(Long companyId) {
        CompanyLogo logo = findLogoOrThrow(companyId);
        companyLogoRepository.delete(logo);
    }


    private void validateCompanyExists(Long companyId) {
        if (!companyRepository.existsById(companyId)) {
            throw new NotFoundException("Empresa não encontrada para o ID: " + companyId);
        }
    }

    private CompanyLogo findLogoOrThrow(Long companyId) {
        return companyLogoRepository.findByCompanyId(companyId)
                .orElseThrow(() -> new NotFoundException(
                        "Logo não encontrado para a empresa ID: " + companyId
                ));
    }


}

