package org.hriday.archiveFile;

import org.hriday.factory.Archive;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;


public class ArchiveFile implements Archive {

    @Override
    public void compress(String sourceFile, String compressFile) throws IOException {

        String[] extension = compressFile.split("\\.");
        String fileName = extension[0] + ".zip";

        byte[] buffer = new byte[10000];

        try {
            FileOutputStream out = new FileOutputStream(fileName);
            ZipOutputStream zipOut = new ZipOutputStream(out);

            File folder = new File(sourceFile);
            for (File file : folder.listFiles()) {
                if (!file.isDirectory()) {
                    FileInputStream fileInputStream = new FileInputStream(file);
                    zipOut.putNextEntry(new ZipEntry(file.getName()));
                    int length;

                    while ((length = fileInputStream.read(buffer)) > 0) {
                        zipOut.write(buffer, 0, length);
                    }
                    fileInputStream.close();
                }

            }
            zipOut.closeEntry();
            zipOut.close();
            out.close();


        } catch (FileNotFoundException e) {

            throw new FileNotFoundException(e.getMessage());
        }

    }

    @Override
    public void decompress(String fileZip, String unzipFile) throws IOException {

        byte[] buffer = new byte[100];

        try {
            FileInputStream fileInputStream = new FileInputStream(fileZip);
            ZipInputStream zipInputStream = new ZipInputStream(fileInputStream);
            ZipEntry zipEntry = zipInputStream.getNextEntry();
            while (zipEntry != null) {
                String fileName = zipEntry.getName();
                File newFile = new File(unzipFile + File.separator + fileName);
                new File(newFile.getParent()).mkdirs();
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zipInputStream.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
                zipInputStream.closeEntry();
                zipEntry = zipInputStream.getNextEntry();
            }
            zipInputStream.closeEntry();
            zipInputStream.close();
            fileInputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
