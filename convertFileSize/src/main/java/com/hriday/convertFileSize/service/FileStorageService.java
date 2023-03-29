package com.hriday.convertFileSize.service;

import com.hriday.convertFileSize.dao.ArchiveDetails;
import com.hriday.convertFileSize.dto.ArchiveDetailsDto;
import com.hriday.convertFileSize.exception.CustomException;
import com.hriday.convertFileSize.factory.ArchiveService;
import com.hriday.convertFileSize.repository.ArchiveRepo;
import com.hriday.convertFileSize.utils.ErrorMessage;
import com.hriday.convertFileSize.utils.FileType;
import com.hriday.convertFileSize.utils.Status;
import org.hriday.archiveFile.ArchiveFile;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class FileStorageService implements ArchiveService {

    @Autowired
    protected ArchiveRepo archiveRepo;

    @Value("${fileStorage}")
    protected String fileStoragePath;

    @Value("${tempStorage}")
    public String tempStoragePath;

    ArchiveFile archiveFile = new ArchiveFile();

    @Override
    public String compress(MultipartFile[] file) throws IOException {

        String fileName = StringUtils.cleanPath(Objects.requireNonNull(file[0].getOriginalFilename()));

        String[] extension = fileName.split("\\.");
        FileType fileTypeName = FileType.valueOf(extension[extension.length - 1].toUpperCase());
        EnumSet<FileType> fileTypes = EnumSet.allOf(FileType.class);

        if (!fileTypes.contains(fileTypeName)) throw new CustomException(ErrorMessage.TYPE_NOT_FOUND);

        String tempFilePath = tempStoragePath + "\\" + extension[0];
        File folder = new File(tempFilePath);
        folder.mkdir();

        convertMultipartFileToFile(file, tempFilePath);

        String compressedFileName = fileName.substring(0, fileName.length() - 3);

        String compressedFilePath = fileStoragePath + "\\" + compressedFileName + fileTypeName.toString().toLowerCase();

        archiveFile.compress(tempFilePath, compressedFilePath);

        return logsInDatabase(compressedFileName + fileTypeName.toString().toLowerCase());
    }

    public String logsInDatabase(String fileName) {

        ArchiveDetailsDto archiveDetailsDto = ArchiveDetailsDto
                .builder()
                .fileName(fileName)
                .uid(String.valueOf(UUID.randomUUID()))
                .uploadedAt(System.currentTimeMillis())
                .status(Status.UPLOADED.toString())
                .build();

        ArchiveDetails archiveDetails = new ArchiveDetails();
        BeanUtils.copyProperties(archiveDetailsDto, archiveDetails);

        archiveRepo.save(archiveDetails);

        return archiveDetailsDto.getUid();
    }

    @Override
    public String decompress(MultipartFile[] file) throws IOException {

        String fileName = StringUtils.cleanPath(Objects.requireNonNull(file[0].getOriginalFilename()));

        String tempFilePath = tempStoragePath + "\\" + fileName;

        convertMultipartFileToFile(file, tempFilePath);

        String decompressedFilepath = fileStoragePath;

        archiveFile.decompress(tempFilePath, decompressedFilepath);

        FileInputStream fileInputStream = new FileInputStream(tempFilePath);
        ZipInputStream zis = new ZipInputStream(fileInputStream);
        ZipEntry zipEntry = zis.getNextEntry();

        return logsInDatabase(zipEntry.getName());
    }

    @Override
    public void convertMultipartFileToFile(MultipartFile[] multipartFiles, String tempFilePath) throws IOException {

        for (MultipartFile file : multipartFiles) {
            file.getName();
            String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

            File tempFile = new File(tempFilePath+"\\"+fileName);

            file.transferTo(tempFile);

            scheduleFileDeletion(tempFile, 200 * 1000);

        }
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


    public Resource downloadFile(String uid) throws IOException {

        ArchiveDetails archiveDetails = archiveRepo.findByUid(uid);
        String[] extension = archiveDetails.getFileName().split("\\.");
        String compress = extension[0] + ".zip";

        Path path = Paths.get(fileStoragePath).toAbsolutePath().resolve(compress);
        Resource resource;

        try {
            resource = new UrlResource(path.toUri());
        } catch (MalformedURLException e) {
            throw new CustomException(ErrorMessage.NOT_READABLE);
        }

        if (resource.exists() && resource.isReadable()) {
            logAfterDownload(archiveDetails);
            return resource;
        } else {
            throw new CustomException(ErrorMessage.FILE_NOT_EXIST);
        }
    }

    private void logAfterDownload(ArchiveDetails archiveDetails) {
        archiveDetails.setStatus(Status.DOWNLOADED.toString());
        archiveDetails.setDownloadedAt(System.currentTimeMillis());
        archiveRepo.save(archiveDetails);
    }

}
