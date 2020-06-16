package com.android.mydownloader.Util;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.mydownloader.R;
import com.android.mydownloader.db.FileInfo;

import org.litepal.LitePal;

public class DownloadView extends RelativeLayout {

    private TextView fileNameText;
    public TextView progressText;
    public ProgressBar progressBar;
    public ImageView startView;
    public ImageView pauseView;
    private ImageView cancelView;

    public DownloadView(Context context, final FileInfo info) {
        super(context);
        final String downloadUrl = info.getDownloadUrl();
        final String fileName = info.getFileName();
        int progress = (int) (info.getDownloadLength() * 100 / info.getContentLength());
        View view = inflate(context,R.layout.downloading_item,this);
        fileNameText = (TextView)view.findViewById(R.id.file_name);
        progressText = (TextView)view.findViewById(R.id.progress);
        progressBar = (ProgressBar)view.findViewById(R.id.download_progress);
        startView = (ImageView)view.findViewById(R.id.start);
        pauseView = (ImageView)view.findViewById(R.id.pause);
        cancelView = (ImageView)view.findViewById(R.id.cancel);
        fileNameText.setText(fileName);
        if (info.isPause()){
            progressText.setText("暂停下载");
            progressBar.setProgress(progress);
            startView.setVisibility(VISIBLE);
            pauseView.setVisibility(INVISIBLE);
        }else {
            progressText.setText(progress + "%");
            progressBar.setProgress(progress);
            pauseView.setVisibility(VISIBLE);
            startView.setVisibility(INVISIBLE);

        }
        startView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                FileInfo file = LitePal.where("downloadUrl = ?",downloadUrl).findFirst(FileInfo.class);
                DownloadUtil.getDownloadUtil().startDownload(file);
                pauseView.setVisibility(VISIBLE);
                startView.setVisibility(INVISIBLE);
            }
        });
        pauseView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                DownloadUtil.getDownloadUtil().pauseDownload(downloadUrl);
                startView.setVisibility(VISIBLE);
                pauseView.setVisibility(INVISIBLE);
            }
        });
        cancelView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                DownloadUtil.getDownloadUtil().cancelDownload(downloadUrl,fileName);
            }
        });
    }

}
