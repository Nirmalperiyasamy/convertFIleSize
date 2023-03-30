package com.hriday.convertFileSize.repository;

import com.hriday.convertFileSize.dao.ArchiveDetails;
import com.hriday.convertFileSize.utils.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface ArchiveRepo extends JpaRepository<ArchiveDetails, Integer> {

    ArchiveDetails findByUid(String uid);

    List<ArchiveDetails> findByStatusAndUploadedAtLessThan(Status uploaded, Long l);
}
