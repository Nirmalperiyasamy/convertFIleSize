package org.hriday.factory;

import java.io.File;
import java.io.IOException;

public interface ArchiveMethods {

    void compress(String sourceFile, String compressFile) throws IOException;

    void decompress(String fileZip, File destinationDirectory) throws IOException;
}
