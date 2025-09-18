package com.montreal.msiav_bh.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileReportResponse {
	
    private Long id;
    private String receivedDate;
    private String fileName;
    private String fileType;
    private String filePath;
    private Long reportId;
}
