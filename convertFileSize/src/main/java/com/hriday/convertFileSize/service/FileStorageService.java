package com.hriday.convertFileSize.service;

import com.hriday.convertFileSize.constValues.Const;
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
import org.springframework.scheduling.annotation.EnableScheduling;
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
@EnableScheduling
public class FileStorageService implements ArchiveService {

    @Autowired
    protected ArchiveRepo archiveRepo;

    @Value("${fileStorage}")
    protected String fileStoragePath;

    @Value("${tempStorage}")
    public String tempStoragePath;

    @Autowired
    protected ArchiveFile archiveFile;

    @Override
    public String compress(MultipartFile[] file) throws IOException {

        String fileName = StringUtils.cleanPath(Objects.requireNonNull(file[0].getOriginalFilename()));

        String[] fileNameSep = fileName.split("\\.");
        FileType fileTypeName = FileType.valueOf(fileNameSep[fileNameSep.length - 1].toUpperCase());
        EnumSet<FileType> fileTypes = EnumSet.allOf(FileType.class);

        if (!fileTypes.contains(fileTypeName)) throw new CustomException(ErrorMessage.TYPE_NOT_FOUND);

        String tempFilePath = tempStoragePath + File.separator + fileNameSep[0];
        File folder = new File(tempFilePath);
        folder.mkdir();

        convertMultipartFileToFile(file, tempFilePath);

        String compressedFilePath = fileStoragePath + File.separator + fileName;

        archiveFile.compress(tempFilePath, compressedFilePath);

        return logsInDatabase(fileNameSep[0]);
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

        String tempFilePath = tempStoragePath + File.separator + fileName;

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

            File tempFile = new File(tempFilePath + File.separator + fileName);

            file.transferTo(tempFile);

            scheduleFileDeletion(tempFile, 20 * 1000);
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

        Path path = Paths.get(fileStoragePath).toAbsolutePath().resolve(archiveDetails.getFileName() + Const.ZIP);
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
