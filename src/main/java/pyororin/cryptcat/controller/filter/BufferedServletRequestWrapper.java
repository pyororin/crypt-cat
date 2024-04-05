package pyororin.cryptcat.controller.filter;

import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class BufferedServletRequestWrapper extends HttpServletRequestWrapper {

    private final byte[] buffer;

    public BufferedServletRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);

        // Request BodyからStreamを取得
        var is = request.getInputStream();

        // Streamをbyte配列に変換し、インスタンス変数に保持
        var baos = new ByteArrayOutputStream();
        byte[] buff = new byte[1024];
        int read;
        while ((read = is.read(buff)) > 0) {
            baos.write(buff, 0, read);
        }

        this.buffer = baos.toByteArray();
    }

    // Bodyの取得元をこのメソッドに差替え
    @Override
    public ServletInputStream getInputStream() {
        // Streamクラスを初期化して返却
        return new BufferedServletInputStream(this.buffer);
    }
}
