package com.hriday.convertFileSize.dao;

import com.hriday.convertFileSize.utils.Status;
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

    private String fileName;

    @Enumerated(EnumType.STRING)
    private Status status;

    private String uid;
    @Enumerated(EnumType.STRING)
    private Status tempStatus;

}
