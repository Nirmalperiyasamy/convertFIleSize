package org.hriday.archiveFile;

import org.hriday.archiveImage.ImageCompressor;
import org.hriday.archiveTxt.TxtCompressor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ArchiveFile {
    public void compress(String sourceFile, String compressFile) throws
            IOException {

        String[] extension = compressFile.split("\\.");

        switch (extension[1]) {
            case "txt":
                TxtCompressor txtCompressor = new TxtCompressor();
                txtCompressor.compress(sourceFile, compressFile);
                break;

            case "jpg":
                ImageCompressor imageCompressor = new ImageCompressor();
                imageCompressor.compress(sourceFile, compressFile);
                break;
        }

    }

    public void decompress(String fileZip, File destinationDirectory) throws IOException {

        byte[] buffer = new byte[1024];

        ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(fileZip));
        ZipEntry zipEntry = zipInputStream.getNextEntry();

        while (zipEntry != null) {
            File newFile = newFileCreation(destinationDirectory, zipEntry);
            if (zipEntry.isDirectory()) {
                if (!newFile.isDirectory() && !newFile.mkdirs()) {
                    throw new IOException("Failed to create directory " + newFile);
                }
            } else {
                File parent = newFile.getParentFile();
                if (!parent.isDirectory() && !parent.mkdirs()) {
                    throw new IOException("Failed to create directory " + parent);
                }

                FileOutputStream fileOutputStream = new FileOutputStream(newFile);
                int length;
                while ((length = zipInputStream.read(buffer)) > 0) {
                    fileOutputStream.write(buffer, 0, length);
                }
                fileOutputStream.close();
            }

            zipEntry = zipInputStream.getNextEntry();
        }

        zipInputStream.closeEntry();
        zipInputStream.close();
    }

    public static File newFileCreation(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }
}
