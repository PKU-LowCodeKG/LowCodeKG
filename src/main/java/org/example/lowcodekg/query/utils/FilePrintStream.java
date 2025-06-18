package org.example.lowcodekg.query.utils;

import java.io.*;

public class FilePrintStream extends PrintStream {
    public FilePrintStream(String filePath) throws IOException {
        super(new FileOutputStream(filePath, true)); // 打开文件流，追加模式
    }

    @Override
    public void close() {
        super.close();
    }
}