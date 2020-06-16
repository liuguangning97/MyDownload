package com.android.mydownloader;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.mydownloader.Util.DownloadUtil;
import com.android.mydownloader.Util.DownloadView;
import com.android.mydownloader.Util.FinishAdapter;
import com.android.mydownloader.db.DownloadInfo;
import com.android.mydownloader.db.FileInfo;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private EditText editPath;
    private EditText countText;
    private ImageView start;
    private LinearLayout downloadView;
    private RecyclerView finishView;
    private String downloadUrl;
    private DownloadView view;

    private FinishAdapter finishAdapter;
    private List<FileInfo> finishList = new ArrayList<>();

    private Map<String,DownloadView> infoMap = new LinkedHashMap<>();

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case DownloadUtil.TYPE_START:
                    downloadUrl = (String) msg.obj;
                    if (infoMap.get(downloadUrl) == null ){
                        FileInfo info = LitePal.where("downloadUrl = ?",downloadUrl).findFirst(FileInfo.class);
                        view = new DownloadView(MainActivity.this,info);
                        infoMap.put(downloadUrl, view);
                        downloadView.addView(view);
                    }else {
                        view = infoMap.get(downloadUrl);
                        view.startView.setVisibility(View.INVISIBLE);
                        view.pauseView.setVisibility(View.VISIBLE);
                    }
                    Toast.makeText(MainActivity.this,"开始下载",Toast.LENGTH_SHORT).show();
                    break;
                case DownloadUtil.TYPE_PROGRESS:
                    downloadUrl = (String)msg.obj;
                        view = infoMap.get(downloadUrl);
                        view.progressText.setText(msg.arg1 + "%");
                        view.progressBar.setProgress(msg.arg1);
                    break;
                case DownloadUtil.TYPE_SUCCESS:
                    refreshFinishView();
                    downloadUrl = (String)msg.obj;
                    view = infoMap.get(downloadUrl);
                    infoMap.remove(downloadUrl);
                    downloadView.removeView(view);
                    Toast.makeText(MainActivity.this,"下载完成",Toast.LENGTH_SHORT).show();
                    break;
                case DownloadUtil.TYPE_FAILED:
                    downloadUrl = (String)msg.obj;
                    view = infoMap.get(downloadUrl);
                    view.progressText.setText("暂停下载");
                    Toast.makeText(MainActivity.this,"下载失败",Toast.LENGTH_SHORT).show();
                    break;
                case DownloadUtil.TYPE_PAUSE:
                    downloadUrl = (String)msg.obj;
                    view = infoMap.get(downloadUrl);
                    view.progressText.setText("暂停下载");
                    Toast.makeText(MainActivity.this,"暂停下载",Toast.LENGTH_SHORT).show();
                    break;
                case DownloadUtil.TYPE_CANCEL:
                    refreshFinishView();
                    downloadUrl = (String)msg.obj;
                    view = infoMap.get(downloadUrl);
                    infoMap.remove(downloadUrl);
                    downloadView.removeView(view);
                    Toast.makeText(MainActivity.this,"取消下载",Toast.LENGTH_SHORT).show();
                    break;
                case DownloadUtil.TYPE_DELETE:
                    refreshFinishView();
                    Toast.makeText(MainActivity.this,"已删除文件",Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DownloadUtil.mHandler = mHandler;
        setAllPause();
        editPath = (EditText)findViewById(R.id.edit_path);
        countText = (EditText)findViewById(R.id.count);
        downloadView = (LinearLayout)findViewById(R.id.downloading_view);
        List<FileInfo> infoList = LitePal.where("isFinish = ?","0").find(FileInfo.class);
        for (FileInfo info : infoList){
            view = new DownloadView(MainActivity.this,info);
            infoMap.put(info.getDownloadUrl(),view);
            downloadView.addView(view);
        }
        finishView = (RecyclerView)findViewById(R.id.finish_recycler_view);
        LinearLayoutManager finishManager = new LinearLayoutManager(this);
        finishView.setLayoutManager(finishManager);
        refreshFinishView();
        start = (ImageView) findViewById(R.id.start_download);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String downloadUrl = editPath.getText().toString();
                String count = countText.getText().toString();
                if (downloadUrl.equals("") || count.equals("")){
                    Toast.makeText(MainActivity.this,"请输入完整信息！",Toast.LENGTH_SHORT).show();
                }else {
                    DownloadUtil.getDownloadUtil().createFileInfo(downloadUrl,Integer.valueOf(count));
                }
            }
        });
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(MainActivity.this,"拒绝权限无法使用本程序！",Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    private void refreshFinishView() {
        finishList = LitePal.where("isFinish = ?", "1").find(FileInfo.class);
        finishAdapter = new FinishAdapter(finishList);
        finishView.setAdapter(finishAdapter);
    }

    private void setAllPause(){
        DownloadInfo downloadInfo = new DownloadInfo();
        downloadInfo.setPause(true);
        downloadInfo.updateAll();
        FileInfo fileInfo = new FileInfo();
        fileInfo.setPause(true);
        fileInfo.updateAll();
    }
}
