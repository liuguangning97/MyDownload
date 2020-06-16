package com.android.mydownloader.db;

import org.litepal.crud.LitePalSupport;

public class FileInfo extends LitePalSupport {

    private String downloadUrl;
    private String fileName;
    private String path;
    private long downloadLength;
    private boolean isPause;
    private boolean isFinish;
    private long contentLength;
    private int threadCount;

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setdownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getDownloadLength() {
        return downloadLength;
    }

    public void setDownloadLength(long downloadLength) {
        this.downloadLength = downloadLength;
    }

    public boolean isPause() {
        return isPause;
    }

    public void setPause(boolean pause) {
        isPause = pause;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public boolean isFinish() {
        return isFinish;
    }

    public void setFinish(boolean finish) {
        isFinish = finish;
    }

    public long getContentLength() {
        return contentLength;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    public int getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }

    @Override
    public String toString() {
        return "FileInfo{" +
                "downloadUrl='" + downloadUrl + '\'' +
                ", fileName='" + fileName + '\'' +
                ", path='" + path + '\'' +
                ", downloadLength=" + downloadLength +
                ", isPause=" + isPause +
                ", isFinish=" + isFinish +
                ", contentLength=" + contentLength +
                '}';
    }
}
