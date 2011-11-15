package ssi.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;


public class FileDocumentParser {

    public static Document parseFile(URI file) throws IOException {
        return parseFile(new File(file));
    }

    public static Document parseFile(File file) throws IOException {
        InputStream in = null;
        try {
            in = new FileInputStream(file);
            long length = file.length();
            return parseFile(in, length > 10000 ? 10000 : (int)length);
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    public static Document parseFile(InputStream in, int capacity) throws IOException {
        DocumentParser documentParser = new DocumentParser(new DocumentParser.Builder().setPlainBufferCapacity(capacity));
        byte[] bytes = new byte[2048];
        int read;
        while ((read = in.read(bytes)) != -1) {
            documentParser.parse(bytes, read);
        }
        return documentParser.finish();
    }

}
