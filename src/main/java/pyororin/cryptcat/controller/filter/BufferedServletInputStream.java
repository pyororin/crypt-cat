package pyororin.cryptcat.controller.filter;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;

public class BufferedServletInputStream extends ServletInputStream {

    private final ByteArrayInputStream inputStream;

    // byte配列で初期化
    public BufferedServletInputStream(byte[] buffer) {
        this.inputStream = new ByteArrayInputStream(buffer);
    }

    @Override
    public int available() {
        return inputStream.available();
    }

    @Override
    public int read() {
        return inputStream.read();
    }

    @Override
    public int read(@NotNull byte[] b, int off, int len) {
        return inputStream.read(b, off, len);
    }

    @Override
    public boolean isFinished() {
        return false;
    }

    @Override
    public boolean isReady() {
        return false;
    }

    @Override
    public void setReadListener(ReadListener listener) {

    }
}