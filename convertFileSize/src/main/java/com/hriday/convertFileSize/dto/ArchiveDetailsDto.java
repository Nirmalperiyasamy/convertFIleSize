package com.hriday.convertFileSize.dto;

import lombok.Builder;
import lombok.Data;

@Data

public class ArchiveDetailsDto {
    private Integer id;

    private Long uploadedAt;

    private Long downloadedAt;

    private Long compressedSize;

    private String fileName;

    private String status;

    private String uid;

    private Long uploadedSize;
}
