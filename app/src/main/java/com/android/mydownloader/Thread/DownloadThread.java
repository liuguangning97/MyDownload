package com.android.mydownloader.Thread;

import android.util.Log;

import com.android.mydownloader.Util.DownloadUtil;
import com.android.mydownloader.db.DownloadInfo;

import org.litepal.LitePal;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DownloadThread extends Thread {

    private String downloadUrl;
    private long startIndex;
    private long endIndex;
    private long downloadLength;
    private int threadId;
    private String path;

    public DownloadThread(String downloadUrl, long startIndex, long endIndex, long downloadLength, int threadId, String path) {
        this.downloadUrl = downloadUrl;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.downloadLength = downloadLength;
        this.threadId = threadId;
        this.path = path;
    }

    @Override
    public void run() {
        RandomAccessFile saveFile = null;
        InputStream is = null;
        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .addHeader("RANGE","bytes=" + (startIndex + downloadLength) + "-" + endIndex)
                    .url(downloadUrl)
                    .build();
            Response response = client.newCall(request).execute();
            if (response != null ){
                is = response.body().byteStream();
                File file = new File(path);
                saveFile = new RandomAccessFile(file,"rw");
                saveFile.seek(startIndex + downloadLength);
                int len;
                byte[] b = new byte[1024*1024];
                while ((len = is.read(b)) != -1 ){
                    if (DownloadUtil.getDownloadUtil().checkPause(downloadUrl,threadId)){
                        DownloadUtil.getDownloadUtil().stop(downloadUrl,path);
                        return;
                    }
                    saveFile.write(b,0,len);
                    downloadLength += len;
                    saveDownload(downloadLength);
                }
                DownloadUtil.getDownloadUtil().success(downloadUrl);
            }
            response.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null ){
                    is.close();
                }
                if (saveFile != null ){
                    saveFile.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveDownload(long downloadLength){
        DownloadInfo info = new DownloadInfo();
        info.setDownloadLength(downloadLength);
        info.updateAll("downloadUrl = ? and threadId = ?",downloadUrl,String.valueOf(threadId));
        DownloadUtil.getDownloadUtil().setProgress(downloadUrl);
    }
}
