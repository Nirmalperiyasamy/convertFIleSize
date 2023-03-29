package com.hriday.convertFileSize.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
public class ArchiveDetailsDto {

    private Integer id;

    private Long uploadedAt;

    private Long downloadedAt;

    private String fileName;

    private String status;

    private String uid;


}
