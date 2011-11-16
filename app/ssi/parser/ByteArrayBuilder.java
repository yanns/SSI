package ssi.parser;

import java.util.Arrays;

public class ByteArrayBuilder {
    private byte[] byteBuffer;
    private final int initialCapacity;
    int length = 0;

    public ByteArrayBuilder(int initialCapacity) {
        this.initialCapacity = initialCapacity;
        this.byteBuffer = new byte[initialCapacity];
    }

    public void append(byte b) {
        ensureCapacity(length + 1);
        byteBuffer[length++] = b;
    }

    public void append(ByteArrayBuilder b) {
        append(b.byteBuffer, b.length);
    }

    public void append(byte[] src, int srcLength) {
        ensureCapacity(length + srcLength);
        System.arraycopy(src, 0, byteBuffer, length, srcLength);
        length += srcLength;
    }

    public void clear() {
        length = 0;
    }

    public byte[] getByteBuffer() {
        return Arrays.copyOf(byteBuffer, length);
    }

    private void ensureCapacity(int minimumCapacity) {
        if (minimumCapacity > byteBuffer.length)
            byteBuffer = Arrays.copyOf(byteBuffer, Math.max(byteBuffer.length + initialCapacity, minimumCapacity)); // add more capacity
    }

    @Override
    public String toString() {
        return new String(byteBuffer);
    }

}
