package com.hriday.convertFileSize.controller;

import com.hriday.convertFileSize.service.FileStorageService;
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

@RestController
@RequestMapping
public class FileController {

    private final FileStorageService fileStorageService;

    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping("/compress")
    public ResponseEntity<?> fileUpload(@RequestParam("file") MultipartFile file) throws IOException {

        String fileName = fileStorageService.compressFile(file);

        String url = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/download/")
                .path(fileName)
                .toUriString();

        String contentType = file.getContentType();

        return ResponseEntity.ok().body(url);
    }
    @PostMapping("/decompress")
    public ResponseEntity<?> decompress(@RequestParam("file") MultipartFile file) throws IOException {

        String fileName = fileStorageService.decompressFile(file);

        String url = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/download/")
                .path(fileName)
                .toUriString();

        String contentType = file.getContentType();

        return ResponseEntity.ok().body(url);
    }

    @GetMapping("/download/{uid}")
    public ResponseEntity<?> download(@PathVariable String uid, HttpServletResponse response) throws IOException {

        Resource resource = fileStorageService.downloadFile(uid);

        try (ZipOutputStream zipOutputStream = new ZipOutputStream(response.getOutputStream())) {

            ZipEntry zipEntry = new ZipEntry(Objects.requireNonNull(resource.getFilename()));

            try {
                zipEntry.setSize(resource.contentLength());
                zipOutputStream.putNextEntry(zipEntry);

                StreamUtils.copy(resource.getInputStream(), zipOutputStream);

                zipOutputStream.closeEntry();

            } catch (IOException e) {
                System.out.println("some exception while ziping");
            }

            zipOutputStream.finish();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return ResponseEntity.ok().body("zip file downloading");

    }
}
