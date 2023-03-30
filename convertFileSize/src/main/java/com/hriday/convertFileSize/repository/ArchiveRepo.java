package com.hriday.convertFileSize.repository;

import com.hriday.convertFileSize.dao.ArchiveDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface ArchiveRepo extends JpaRepository<ArchiveDetails, Integer> {

    ArchiveDetails findByUid(String uid);

    List<ArchiveDetails> findByTempStatusAndUploadedAtLessThan(Enum status, Long l);
}
