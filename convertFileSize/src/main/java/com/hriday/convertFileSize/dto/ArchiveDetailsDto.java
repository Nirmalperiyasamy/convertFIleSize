package com.hriday.convertFileSize.dto;

import com.hriday.convertFileSize.utils.Status;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Data
@Builder
public class ArchiveDetailsDto {

    private Integer id;

    private Long uploadedAt;

    private Long downloadedAt;

    private String fileName;
    @Enumerated(EnumType.STRING)
    private Status status;

    private String uid;
    @Enumerated(EnumType.STRING)
    private Status tempStatus;


}
