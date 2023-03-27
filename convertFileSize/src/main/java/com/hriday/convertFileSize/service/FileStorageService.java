package com.hriday.convertFileSize.service;

import com.hriday.convertFileSize.dao.ArchiveDetails;
import com.hriday.convertFileSize.dto.ArchiveDetailsDto;
import com.hriday.convertFileSize.factory.ArchiveService;
import com.hriday.convertFileSize.globalException.CustomException;
import com.hriday.convertFileSize.repository.ArchiveRepo;
import com.hriday.convertFileSize.utils.FileType;
import org.hriday.archiveFile.ArchiveFile;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FileStorageService implements ArchiveService {


    protected final ArchiveRepo archiveRepo;
    protected final Path fileStoragePath;
    protected final String fileStorageLocation;
    protected final Path tempStoragePath;
    protected final String tempStorageLocation;

    ArchiveFile archiveFile = new ArchiveFile();

    public FileStorageService(ArchiveRepo archiveRepo, @Value("${file.storage.location1}") String fileStorageLocation, @Value("${file.storage.location2}") String tempStorageLocation) {


        this.archiveRepo = archiveRepo;

        this.fileStorageLocation = fileStorageLocation;
        fileStoragePath = Paths.get(fileStorageLocation).toAbsolutePath().normalize();

        this.tempStorageLocation = tempStorageLocation;
        tempStoragePath = Paths.get(tempStorageLocation).toAbsolutePath().normalize();

        try {
            Files.createDirectories(fileStoragePath);
            Files.createDirectories(tempStoragePath);
        } catch (IOException e) {
            throw new RuntimeException("Issue in creating file directory");
        }
    }

    @Override
    public String compressFile(MultipartFile file) throws IOException {

        String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

        String[] extension = fileName.split("\\.");

        EnumSet<FileType> fileTypes = EnumSet.allOf(FileType.class);
        Set<String> stringSet = fileTypes.stream().map(Enum::name).collect(Collectors.toSet());

        if (!stringSet.contains(extension[extension.length - 1].toUpperCase())) {
            throw new CustomException("Enum file not found");
        }

        FileType fileTypeName = FileType.valueOf(extension[extension.length - 1].toUpperCase());

        File tempFile = convertMultipartFileToFile(file);

        String tempFilePath = tempStoragePath + "\\" + tempFile.getName();

        String compressedFileName = fileName.substring(0, fileName.length() - 3);

        String compressedFilePath = null;
        File compressedFile = null;

        switch (fileTypeName) {

            case TXT:
                compressedFilePath = fileStoragePath + "\\" + compressedFileName + "zip";
                compressedFile = new File(compressedFilePath);
                break;

            case JPG:
                compressedFilePath = fileStoragePath + "\\" + compressedFileName + fileTypeName.toString().toLowerCase();
                compressedFile = new File(compressedFilePath);
                break;
        }

        archiveFile.compress(tempFilePath, compressedFile);

        assert compressedFile != null;
        return logsInDatabase(tempFile, compressedFile);
    }

    public String logsInDatabase(File tempFile, File compressedFile) {

        ArchiveDetailsDto archiveDetailsDto = new ArchiveDetailsDto();
        archiveDetailsDto.setFileName(compressedFile.getName());
        archiveDetailsDto.setUploadedAt(System.currentTimeMillis());
        archiveDetailsDto.setUploadedSize(tempFile.length());
        archiveDetailsDto.setCompressedSize(compressedFile.length());
        archiveDetailsDto.setUid(String.valueOf(UUID.randomUUID()));
        archiveDetailsDto.setStatus("Uploaded");
        ArchiveDetails archiveDetails = new ArchiveDetails();
        BeanUtils.copyProperties(archiveDetailsDto, archiveDetails);

        archiveRepo.save(archiveDetails);

        return archiveDetailsDto.getUid();
    }

    @Override
    public String decompressFile(MultipartFile file) throws IOException {

        File tempFile = convertMultipartFileToFile(file);

        String tempFilePath = tempStoragePath + "\\" + tempFile.getName();

        String zipFileName = tempFile.getName().substring(0, tempFile.getName().length() - 3);

        String decompressedFilepath = String.valueOf(fileStoragePath);

        File decompressedFile = new File(decompressedFilepath);

        archiveFile.decompress(tempFilePath, decompressedFile);

        return zipFileName + "txt";
    }

    @Override
    public File convertMultipartFileToFile(MultipartFile file) throws IOException {

        String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

        File tempFile = new File(tempStoragePath + "\\" + fileName);
        file.transferTo(tempFile);

        scheduleFileDeletion(tempFile, 20 * 1000);

        FileOutputStream fos = new FileOutputStream(tempFile);
        fos.write(file.getBytes());
        fos.close();

        return tempFile;
    }

    @Override
    public void scheduleFileDeletion(File file, long delayMillis) {

        TimerTask task = new TimerTask() {
            public void run() {
                file.delete();
            }
        };
        Timer timer = new Timer();
        timer.schedule(task, delayMillis);
    }

    @Override
    public Resource downloadFile(String uid) throws IOException {

        ArchiveDetails archiveDetails = archiveRepo.findByUid(uid);

        Path path = Paths.get(fileStorageLocation).toAbsolutePath().resolve(archiveDetails.getFileName());
        Resource resource;

        try {
            resource = new UrlResource(path.toUri());
        } catch (MalformedURLException e) {
            throw new RuntimeException("Issue in reading the file", e);
        }

        if (resource.exists() && resource.isReadable()) {
            logAfterDownload(archiveDetails);

            return resource;
        } else {
            throw new RuntimeException("the file doesn't exist or not readable");
        }
    }

    private void logAfterDownload(ArchiveDetails archiveDetails) {
        archiveDetails.setStatus("downloaded");
        archiveDetails.setDownloadedAt(System.currentTimeMillis());
        archiveRepo.save(archiveDetails);
    }

}
