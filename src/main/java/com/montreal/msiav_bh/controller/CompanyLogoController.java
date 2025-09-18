package com.montreal.msiav_bh.controller;

import com.montreal.msiav_bh.dto.CompanyLogoFileDTO;
import com.montreal.msiav_bh.dto.response.CompanyLogoResponse;
import com.montreal.msiav_bh.service.CompanyLogoService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/logo")
@RequiredArgsConstructor
@ApiResponses({
    @ApiResponse(responseCode = "401", description = "Acesso não autorizado"),
    @ApiResponse(responseCode = "404", description = "Recurso não encontrado")
})
public class CompanyLogoController {

    private final CompanyLogoService companyLogoService;

    @PostMapping("/{companyId}/upload")
    public ResponseEntity<CompanyLogoResponse> uploadLogo(
            @PathVariable Long companyId,
            @RequestBody CompanyLogoFileDTO companyLogoFileDTO) {
        CompanyLogoResponse response = companyLogoService.uploadCompanyLogo(companyId, companyLogoFileDTO.file());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{companyId}")
    public ResponseEntity<CompanyLogoFileDTO> getCompanyLogoBase64(@PathVariable Long companyId) {
        CompanyLogoFileDTO companyLogoFileDTO = companyLogoService.getCompanyLogoBase64(companyId);
        return ResponseEntity.ok(companyLogoFileDTO);
    }

    @PutMapping("/{companyId}/upload")
    public ResponseEntity<CompanyLogoResponse> updateLogo(
            @PathVariable Long companyId,
            @RequestBody CompanyLogoFileDTO companyLogoFileDTO) {
        CompanyLogoResponse response =
                companyLogoService.updateCompanyLogo(companyId, companyLogoFileDTO.file());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{companyId}")
    public ResponseEntity<Void> deleteLogo(@PathVariable Long companyId) {
        companyLogoService.deleteCompanyLogo(companyId);
        return ResponseEntity.noContent().build();
    }
}

