package com.hriday.convertFileSize.controller;

import com.hriday.convertFileSize.exception.CustomException;
import com.hriday.convertFileSize.service.FileStorageService;
import com.hriday.convertFileSize.utils.ErrorMessage;
import com.hriday.convertFileSize.utils.SuccessMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.hriday.convertFileSize.constValues.Const.*;

@RequestMapping(API)
@RestController
public class FileController {

    @Autowired
    protected FileStorageService fileStorageService;

    @PostMapping(COMPRESS)
    public ResponseEntity<?> fileUpload(MultipartFile[] file) throws IOException {

        String fileName = fileStorageService.compress(file);

        String url = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/download/")
                .path(fileName)
                .toUriString();

        return ResponseEntity.ok().body(url);
    }

    @PostMapping(DECOMPRESS)
    public ResponseEntity<?> decompress(MultipartFile[] file) throws IOException {

        String fileName = fileStorageService.decompress(file);

        String url = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/download/")
                .path(fileName)
                .toUriString();

        return ResponseEntity.ok().body(url);
    }

    @GetMapping(DOWNLOAD)
    public ResponseEntity<?> download(@PathVariable String uid, HttpServletResponse response) throws IOException {

        Resource resource = fileStorageService.downloadFile(uid);

        ZipOutputStream zipOutputStream = new ZipOutputStream(response.getOutputStream());

        try {
            ZipEntry zipEntry = new ZipEntry((Objects.requireNonNull(resource.getFilename())));

            zipEntry.setSize(resource.contentLength());
            zipOutputStream.putNextEntry(zipEntry);

            StreamUtils.copy(resource.getInputStream(), zipOutputStream);

        } catch (IOException e) {
            throw new CustomException(ErrorMessage.EXCEPTION_WHILE_ZIP);
        } finally {
            zipOutputStream.closeEntry();
            zipOutputStream.finish();
        }

        return ResponseEntity.ok().body(SuccessMessage.DOWNLOAD_SUCCESS);
    }
}

