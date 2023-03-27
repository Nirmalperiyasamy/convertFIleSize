package com.hriday.convertFileSize.dao;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table
@Getter
@Setter
public class ArchiveDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    private Long uploadedAt;

    private Long downloadedAt;

    private Long compressedSize;

    private String fileName;

    private String status;

    private String uid;

    private Long uploadedSize;
}
