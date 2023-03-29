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

        byte[] buffer = new byte[1024];
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

        FileInputStream fis;
        byte[] buffer = new byte[1024];
        try {
            fis = new FileInputStream(fileZip);
            ZipInputStream zis = new ZipInputStream(fis);
            ZipEntry ze = zis.getNextEntry();
            while (ze != null) {
                String fileName = ze.getName();
                File newFile = new File(unzipFile + File.separator + fileName);
                new File(newFile.getParent()).mkdirs();
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
                zis.closeEntry();
                ze = zis.getNextEntry();
            }
            zis.closeEntry();
            zis.close();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
