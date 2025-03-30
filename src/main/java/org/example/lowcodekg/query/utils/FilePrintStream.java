package org.example.lowcodekg.query.utils;

import java.io.*;

public class FilePrintStream extends PrintStream {
    private final BufferedWriter writer;

    public FilePrintStream(String filePath) throws IOException {
        super(new FileOutputStream(filePath, true)); // 打开文件流，追加模式
        this.writer = new BufferedWriter(new FileWriter(filePath, true));
    }

    @Override
    public void write(byte[] buf, int off, int len) {
        super.write(buf, off, len); // 调用父类方法，将内容写入文件
        try {
            writer.write(new String(buf, off, len));
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        super.close();
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}