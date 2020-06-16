package com.android.mydownloader.Util;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.ArrayMap;
import android.util.Log;

import com.android.mydownloader.Thread.DownloadThread;
import com.android.mydownloader.db.DownloadInfo;
import com.android.mydownloader.db.FileInfo;

import org.litepal.LitePal;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DownloadUtil {

    public static final int TYPE_START = 0;
    public static final int TYPE_PROGRESS = 1;
    public static final int TYPE_SUCCESS = 2;
    public static final int TYPE_FAILED = 3;
    public static final int TYPE_PAUSE = 4;
    public static final int TYPE_CANCEL = 5;
    public static final int TYPE_DELETE = 6;

    private static DownloadUtil downloadUtil;

    public static String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();

    public static Handler mHandler;

    private static Map<String,Integer> runningThreadCount = new ArrayMap<>();

    public static Object object = new Object();

    public static DownloadUtil getDownloadUtil(){
        if (downloadUtil == null ){
            downloadUtil = new DownloadUtil();
        }
        return downloadUtil;
    }

    //开始下载
    public void startDownload(FileInfo file){
        String downloadUrl = file.getDownloadUrl();
        String path = file.getPath();
        int threadCount = file.getThreadCount();
        long downloadLength = file.getDownloadLength();
        long contentLength = file.getContentLength();
        if (contentLength == 0 ){
            sendMessage(TYPE_FAILED,-1,downloadUrl);
            return;
        }
        if (contentLength == downloadLength){
            sendMessage(TYPE_SUCCESS,100,downloadUrl);
            return;
        }
        long blockSize = contentLength / threadCount;
        for (int i = 0;i<threadCount;i++){
            long startIndex = i * blockSize;
            long endIndex;
            if (i+1 == threadCount){
                endIndex = contentLength;
            }else {
                endIndex = (i+1) * blockSize - 1;
            }
            DownloadInfo info = setDownloadInfo(downloadUrl,startIndex,endIndex,i+1);
            new DownloadThread(downloadUrl,startIndex,endIndex,info.getDownloadLength(),i+1,path).start();
        }
        runningThreadCount.put(downloadUrl,threadCount);
        sendMessage(TYPE_START,-1,downloadUrl);
    }

    //暂停下载
    public void pauseDownload(String downloadUrl){
        DownloadInfo downloadInfo = new DownloadInfo();
        downloadInfo.setPause(true);
        downloadInfo.updateAll("downloadUrl = ?",downloadUrl);
    }

    //取消下载
    public void cancelDownload(String downloadUrl,String fileName){
        FileInfo info = LitePal.where("downloadUrl = ?",downloadUrl).findFirst(FileInfo.class);
        if (info.isPause()){
            deleteFile(info.getPath());
        }
        LitePal.deleteAll(DownloadInfo.class,"downloadUrl = ?",downloadUrl);
        LitePal.deleteAll(FileInfo.class,"downloadUrl = ?",downloadUrl);
        sendMessage(TYPE_CANCEL,-1,downloadUrl);
    }

    //检测所有线程是否停止并提示下载成功
    public void success(String downloadUrl){
        runningThreadCount.put(downloadUrl,runningThreadCount.get(downloadUrl)-1);
        if (runningThreadCount.get(downloadUrl) == 0){
            runningThreadCount.remove(downloadUrl);
            LitePal.deleteAll(DownloadInfo.class,"downloadUrl = ?",downloadUrl);
            FileInfo info = LitePal.where("downloadUrl = ?",downloadUrl).findFirst(FileInfo.class);
            info.setFinish(true);
            info.save();
            sendMessage(TYPE_SUCCESS,100,downloadUrl);
        }
    }

    //检测所有线程是否停止并提示下载停止
    public void stop(String downloadUrl,String path){
        runningThreadCount.put(downloadUrl,runningThreadCount.get(downloadUrl)-1);
        if (runningThreadCount.get(downloadUrl) == 0 ){
            runningThreadCount.remove(downloadUrl);
            List<DownloadInfo> infoList = LitePal.where("downloadUrl = ?",downloadUrl).find(DownloadInfo.class);
            if (infoList.size() != 0 ){
                FileInfo info = new FileInfo();
                info.setPause(true);
                info.updateAll("downloadUrl = ?",downloadUrl);
                DownloadUtil.getDownloadUtil().sendMessage(DownloadUtil.TYPE_PAUSE,-1,downloadUrl);
            }else {
                deleteFile(path);
            }
         }
    }

    private DownloadInfo setDownloadInfo(String downloadUrl,long startIndex,long endIndex,int threadId){
        DownloadInfo info = LitePal.where("downloadUrl = ? and threadId = ?",downloadUrl,String.valueOf(threadId)).findFirst(DownloadInfo.class);
        if (info == null ){
            info = new DownloadInfo();
            info.setDownloadUrl(downloadUrl);
            info.setStartIndex(startIndex);
            info.setEndIndex(endIndex);
            info.setDownloadLength(0);
            info.setThreadId(threadId);
            info.setToDefault("isPause");
            info.save();
        }else {
            info.setToDefault("isPause");
            info.updateAll("downloadUrl = ? and threadId = ?",downloadUrl,String.valueOf(threadId));
        }
        return info;
    }

    public synchronized void setProgress(String downloadUrl){
            List<DownloadInfo> infoList = LitePal.where("downloadUrl = ?",downloadUrl).find(DownloadInfo.class);
            long total = 0 ;
            for (DownloadInfo info : infoList){
                total += info.getDownloadLength();
            }
            FileInfo info = LitePal.where("downloadUrl = ?",downloadUrl).findFirst(FileInfo.class);
            if (info != null ){
                int progress = (int) (total * 100 / info.getContentLength());
                sendMessage(TYPE_PROGRESS,progress,downloadUrl);
                info.setDownloadLength(total);
                info.save();
            }
    }

    public void createFileInfo(final String downloadUrl, final int threadCount){
        new Thread(new Runnable() {
            @Override
            public void run() {
                long contentLength = 0;
                try {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url(downloadUrl)
                            .build();
                    Response response = client.newCall(request).execute();
                    if (response != null ){
                        contentLength = response.body().contentLength();
                        response.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/"));
                FileInfo info = LitePal.where("downloadUrl = ?",downloadUrl).findFirst(FileInfo.class);
                if (info == null ){
                    info = new FileInfo();
                    info.setdownloadUrl(downloadUrl);
                    info.setPath(directory + fileName);
                    info.setThreadCount(threadCount);
                    info.setDownloadLength(0);
                    info.setFileName(fileName.substring(1));
                    info.setToDefault("isPause");
                    info.setToDefault("isFinish");
                    info.setContentLength(contentLength);
                    info.save();
                }else {
                    info.setToDefault("isPause");
                    info.updateAll("downloadUrl = ?",downloadUrl);
                }
                startDownload(info);
            }
        }).start();
    }

    public void sendMessage(int what,int agr1,String downloadUrl){
        Message m = new Message();
        m.what = what;
        m.arg1 = agr1;
        m.obj = (String)downloadUrl;
        mHandler.sendMessage(m);
    }

    public boolean checkPause(String downloadUrl,int threadId){
        DownloadInfo info = LitePal.where("downloadUrl = ? and threadId = ?",downloadUrl,String.valueOf(threadId)).findFirst(DownloadInfo.class);
        if (info == null ){
            return true;
        }else {
            return info.isPause();
        }
    }

    public void deleteFile(String path) {
        File file = new File(path);
        if (file.exists()){
            file.delete();
        }
    }

}
