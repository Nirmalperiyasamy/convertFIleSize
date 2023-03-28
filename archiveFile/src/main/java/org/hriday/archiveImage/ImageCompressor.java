package org.hriday.archiveImage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ImageCompressor {

    public void compress(String sourceFile, String compressFile) throws IOException {

        String[] extension = compressFile.split("\\.");
        String compress = extension[0] + ".zip";


        File input = new File(sourceFile);
        File output = new File("D:\\outputFile\\" + input.getName());
        BufferedImage image = ImageIO.read(input);
        ImageIO.write(image, "jpg", output);


        byte[] buffer = new byte[1024];

        FileInputStream in = new FileInputStream(output);
        FileOutputStream out = new FileOutputStream(compress);
        ZipOutputStream zipOut = new ZipOutputStream(out);


        zipOut.putNextEntry(new ZipEntry(output.getName()));

        int len;
        while ((len = in.read(buffer)) > 0) {
            zipOut.write(buffer, 0, len);
        }

        in.close();
        zipOut.closeEntry();
        zipOut.close();


    }

}
