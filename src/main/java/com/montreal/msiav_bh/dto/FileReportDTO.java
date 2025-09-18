package com.montreal.msiav_bh.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileReportDTO {
	
    private Long id;
    private String receivedDate;
    private String fileName;
    private String fileType;
    private String filePath;
    private Long reportId;
    private byte[] pdfData;
    
}
