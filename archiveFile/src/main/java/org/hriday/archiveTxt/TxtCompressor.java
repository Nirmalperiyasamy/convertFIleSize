package org.hriday.archiveTxt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class TxtCompressor {
    public void compress(String sourceFile, File compressFile) throws
            IOException {

        FileOutputStream fileOutputStream = new FileOutputStream(compressFile);
        ZipOutputStream zipOut = new ZipOutputStream(fileOutputStream);

        File fileToZip = new File(sourceFile);
        FileInputStream fileInputStream = new FileInputStream(fileToZip);
        ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
        zipOut.putNextEntry(zipEntry);

        byte[] bytes = new byte[1024];
        int length;
        while ((length = fileInputStream.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }

        zipOut.close();
        fileInputStream.close();
        fileOutputStream.close();
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
        System.out.println(destFilePath);
        System.out.println(destDirPath);
        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }
}
