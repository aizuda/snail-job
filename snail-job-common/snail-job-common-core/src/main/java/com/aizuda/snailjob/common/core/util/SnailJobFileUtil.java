package com.aizuda.snailjob.common.core.util;

import com.aizuda.snailjob.common.core.exception.SnailJobInnerExecutorException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class SnailJobFileUtil {

    /**
     * 从给定的 URL 下载文件到本地文件系统
     *
     * @param urlString 要下载的文件的 URL 字符串
     * @param destinationFile 下载后的本地文件
     * @param connectionTimeout 连接超时时间（毫秒）
     * @param readTimeout 读取超时时间（毫秒）
     * @throws IOException 如果发生 IO 错误
     */
    public static void downloadFile(String urlString, File destinationFile, int connectionTimeout, int readTimeout) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(connectionTimeout);
        connection.setReadTimeout(readTimeout);

        try (InputStream inputStream = new BufferedInputStream(connection.getInputStream());
             FileOutputStream fileOS = new FileOutputStream(destinationFile);
             BufferedOutputStream bufferedOutStream = new BufferedOutputStream(fileOS)) {

            byte[] dataBuffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(dataBuffer, 0, 1024)) != -1) {
                bufferedOutStream.write(dataBuffer, 0, bytesRead);
            }
        }  finally {
            connection.disconnect();
        }
    }

    public static File mkdirs(File directory) throws SnailJobInnerExecutorException {
        if (directory != null && !directory.mkdirs() && !directory.isDirectory()) {
            throw new SnailJobInnerExecutorException("Cannot create directory '" + directory + "'.");
        } else {
            return directory;
        }
    }
}
