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
    public String tempStoragePath;

    ArchiveFile archiveFile = new ArchiveFile();

    @Override
    public String compress(MultipartFile[] file) throws IOException {

        String fileName = StringUtils.cleanPath(Objects.requireNonNull(file[0].getOriginalFilename()));

        String[] extension = fileName.split("\\.");

        EnumSet<FileType> fileTypes = EnumSet.allOf(FileType.class);
        Set<String> stringSet = fileTypes.stream().map(Enum::name).collect(Collectors.toSet());

        if (!stringSet.contains(extension[extension.length - 1].toUpperCase())) {
            throw new CustomException("Enum file not found");
        }

        FileType fileTypeName = FileType.valueOf(extension[extension.length - 1].toUpperCase());

        String tempFilePath = tempStoragePath + "\\" + extension[0];
        File folder = new File(tempFilePath);
        folder.mkdir();

        convertMultipartFileToFile(file, tempFilePath);

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

        return logsInDatabase(compressedFile, compressedFile);
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
    public String decompress(MultipartFile[] file) throws IOException {

        String fileName = StringUtils.cleanPath(Objects.requireNonNull(file[0].getOriginalFilename()));

        String[] extension = fileName.split("\\.");

        String tempFilePath = tempStoragePath + "\\" + extension[0];
        File folder = new File(tempFilePath);
        folder.mkdir();

        convertMultipartFileToFile(file, tempFilePath);

        String decompressedFilepath = fileStoragePath + "\\";

        File decompressedFile = new File(decompressedFilepath);

        archiveFile.decompress(tempFilePath, decompressedFile);

        return "m";
    }

    @Override
    public void convertMultipartFileToFile(MultipartFile[] multipartFiles, String tempFilePath) throws IOException {

        for (MultipartFile multi : multipartFiles) {
            String fileName = StringUtils.cleanPath(Objects.requireNonNull(multi.getOriginalFilename()));

            File tempFile = new File(tempFilePath + "\\" + fileName);

            multi.transferTo(tempFile);

            scheduleFileDeletion(tempFile, 20 * 1000);

            FileOutputStream fos = new FileOutputStream(tempFile);
            fos.write(multi.getBytes());
            fos.close();

        }
    }

    @Override
    public void scheduleFileDeletion(File file, long delayMillis) {

        TimerTask task = new TimerTask() {
            public void run() {
                System.out.println("deleted");
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
