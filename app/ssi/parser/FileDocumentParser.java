package ssi.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;


public class FileDocumentParser {

    public static Document parseFile(URI file) throws IOException {
        return parseFile(new File(file));
    }

    public static Document parseFile(File file) throws IOException {
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(file, "r");
            final long length = file.length();
            final DocumentParser documentParser = new DocumentParser(new DocumentParser.Builder().setPlainBufferCapacity(length > 10000 ? 10000 : (int)length));
            final byte[] bytes = new byte[2048];
            int read;
            while ((read = raf.read(bytes)) != -1) {
                documentParser.parse(bytes, read);
            }
            return documentParser.finish();
        } finally {
            if (raf != null) raf.close();
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
