package com.hriday.convertFileSize.repository;

import com.hriday.convertFileSize.dao.ArchiveDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArchiveRepo extends JpaRepository<ArchiveDetails, Integer> {

    ArchiveDetails findByUid(String uid);
}
