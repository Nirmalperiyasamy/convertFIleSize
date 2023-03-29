package com.hriday.convertFileSize.factory;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

public interface ArchiveService {
    String compress(MultipartFile[] file) throws IOException;

//    String decompress(MultipartFile file) throws IOException;

    String decompress(MultipartFile[] file) throws IOException;

//    File[] convertMultipartFileToFile(MultipartFile[] file) throws IOException;

    void convertMultipartFileToFile(MultipartFile[] multipartFiles, String tempFilePath) throws IOException;

    void scheduleFileDeletion(File file, long delayMillis);

}
