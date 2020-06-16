package com.android.mydownloader.Util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.RecyclerView;
import android.util.ArrayMap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.mydownloader.R;
import com.android.mydownloader.db.FileInfo;

import org.litepal.LitePal;

import java.io.File;
import java.util.List;
import java.util.Map;

public class FinishAdapter extends RecyclerView.Adapter<FinishAdapter.ViewHolder> {

    private Context context;
    private List<FileInfo> infoList;

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView fileNameText;
        private TextView filePathText;
        private ImageView openFile;
        private ImageView delete;

        public ViewHolder(@NonNull View view) {
            super(view);
            fileNameText = (TextView)view.findViewById(R.id.file_name);
            filePathText = (TextView)view.findViewById(R.id.file_path);
            openFile = (ImageView)view.findViewById(R.id.open_file);
            delete = (ImageView)view.findViewById(R.id.delete);
        }
    }

    public FinishAdapter(List<FileInfo> infoList) {
        this.infoList = infoList;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int i) {
        FileInfo info = infoList.get(i);
        holder.fileNameText.setText(info.getFileName());
        holder.filePathText.setText(info.getPath());

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        context = viewGroup.getContext();
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.finish_download_item,viewGroup,false);
        final ViewHolder holder = new ViewHolder(view);
        holder.openFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                FileInfo info = infoList.get(position);
                File file = new File(info.getPath());
                File parentFile = new File(file.getParent());
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                    Uri uri = FileProvider.getUriForFile(context,"com.android.mydownloader.fileProvider",parentFile);
                    intent.setDataAndType(uri,"*/*");
                }else {
                    intent.setDataAndType(Uri.fromFile(parentFile),"*/*");
                }
                context.startActivity(intent);
            }
        });
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                FileInfo info = infoList.get(position);
                DownloadUtil.getDownloadUtil().deleteFile(info.getPath());
                LitePal.deleteAll(FileInfo.class,"downloadUrl = ? ",info.getDownloadUrl());
                DownloadUtil.getDownloadUtil().sendMessage(DownloadUtil.TYPE_DELETE,-1,info.getDownloadUrl());
            }
        });
        return holder;
    }

    @Override
    public int getItemCount() {
        return infoList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

}
