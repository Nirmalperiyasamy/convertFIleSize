package com.hriday.convertFileSize.service;

import com.hriday.convertFileSize.dao.ArchiveDetails;
import com.hriday.convertFileSize.dto.ArchiveDetailsDto;
import com.hriday.convertFileSize.factory.ArchiveService;
import com.hriday.convertFileSize.globalException.CustomException;
import com.hriday.convertFileSize.repository.ArchiveRepo;
import com.hriday.convertFileSize.utils.FileType;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FileStorageService implements ArchiveService {

    @Autowired
    protected ArchiveRepo archiveRepo;

    @Value("${fileStorage}")
    protected String fileStoragePath;

    @Value("${tempStorage}")
    protected String tempStoragePath;

    ArchiveFile archiveFile = new ArchiveFile();

    @Override
    public String compress(MultipartFile file) throws IOException {

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

            case JPG:

            case EXE:
                compressedFilePath = fileStoragePath + "\\" + compressedFileName + fileTypeName.toString().toLowerCase();
                compressedFile = new File(compressedFilePath);
                break;
        }

        archiveFile.compress(tempFilePath, compressedFilePath);

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
    public String decompress(MultipartFile file) throws IOException {

        File tempFile = convertMultipartFileToFile(file);

        String tempFilePath = tempStoragePath + "\\" + tempFile.getName();

        String zipFileName = tempFile.getName().substring(0, tempFile.getName().length() - 4);

        String decompressedFilepath = fileStoragePath+"\\"+zipFileName;

        File decompressedFile = new File(decompressedFilepath);

        archiveFile.decompress(tempFilePath, decompressedFile);

        return logsInDatabase(tempFile, decompressedFile);
    }

    @Override
    public File convertMultipartFileToFile(MultipartFile file) throws IOException {

        String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

        File tempFile = new File(tempStoragePath + "\\" + fileName);

        file.transferTo(tempFile);

        scheduleFileDeletion(tempFile, 30 * 1000);

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


    public Resource downloadFile(String uid) throws IOException {

        ArchiveDetails archiveDetails = archiveRepo.findByUid(uid);
        String[] extension = archiveDetails.getFileName().split("\\.");
        String compress = extension[0] + ".zip";

        Path path = Paths.get(fileStoragePath).toAbsolutePath().resolve(compress);
        Resource resource;

        try {
            resource = new UrlResource(path.toUri());
        } catch (MalformedURLException e) {
            throw new CustomException("Issue in reading the file");
        }

        if (resource.exists() && resource.isReadable()) {
            logAfterDownload(archiveDetails);
            return resource;
        } else {
            throw new CustomException("the file doesn't exist or not readable");
        }
    }

    private void logAfterDownload(ArchiveDetails archiveDetails) {
        archiveDetails.setStatus("downloaded");
        archiveDetails.setDownloadedAt(System.currentTimeMillis());
        archiveRepo.save(archiveDetails);
    }

}
