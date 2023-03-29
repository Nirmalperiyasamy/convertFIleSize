package org.hriday.archiveFile;

import org.hriday.factory.ArchiveMethods;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ArchiveFile implements ArchiveMethods {

    @Override
    public void compress(String sourceFile, String compressFile) throws IOException {

        String[] extension = compressFile.split("\\.");
        String compress = extension[0] + ".zip";

        byte[] buffer = new byte[1024];

        FileOutputStream out = new FileOutputStream(compress);
        ZipOutputStream zipOut = new ZipOutputStream(out);

        File folder = new File(sourceFile);
        for (File multi : folder.listFiles()) {
            if (!multi.isDirectory()) {
                FileInputStream fileInputStream = new FileInputStream(multi);
                zipOut.putNextEntry(new ZipEntry(multi.getName()));
                int len;
                while ((len = fileInputStream.read(buffer)) > 0) {
                    zipOut.write(buffer, 0, len);
                }

                fileInputStream.close();
            }

        }
        zipOut.closeEntry();
        zipOut.close();
        out.close();

    }

    @Override
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
