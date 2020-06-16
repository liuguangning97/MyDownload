package com.android.mydownloader.db;

import org.litepal.crud.LitePalSupport;

public class DownloadInfo extends LitePalSupport {

    private String downloadUrl;
    private long startIndex;
    private long endIndex;
    private long downloadLength;
    private boolean isPause;
    private int threadId;

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public long getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(long startIndex) {
        this.startIndex = startIndex;
    }

    public long getEndIndex() {
        return endIndex;
    }

    public void setEndIndex(long endIndex) {
        this.endIndex = endIndex;
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

    public int getThreadId() {
        return threadId;
    }

    public void setThreadId(int threadId) {
        this.threadId = threadId;
    }

    @Override
    public String toString() {
        return "DownloadInfo{" +
                "downloadUrl='" + downloadUrl + '\'' +
                ", startIndex=" + startIndex +
                ", endIndex=" + endIndex +
                ", downloadLength=" + downloadLength +
                ", isPause=" + isPause +
                ", threadId=" + threadId +
                '}';
    }
}
