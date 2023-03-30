package org.hriday.factory;

import java.io.File;
import java.io.IOException;

public interface Archive {

    void compress(String sourceFile, String compressFile) throws IOException;

    void decompress(String fileZip, String unzipFile) throws IOException;
}
