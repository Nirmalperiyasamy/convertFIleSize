package com.hriday.convertFileSize.factory;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

public interface ArchiveService {
    String compressFile(MultipartFile file) throws IOException;

    String decompressFile(MultipartFile file) throws IOException;

    File convertMultipartFileToFile(MultipartFile file) throws IOException;

    void scheduleFileDeletion(File file, long delayMillis);

    Resource downloadFile(String fileName) throws IOException;
}
