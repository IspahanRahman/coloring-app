package com.example.coloringapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.coloringapp.Interface.ImageOnClick;
import com.example.coloringapp.R;
import com.example.coloringapp.ViewFileActivity;
import com.example.coloringapp.ViewHolder.WorkViewHolder;

import java.io.File;
import java.util.List;

public class WorkAdapter extends RecyclerView.Adapter<WorkViewHolder> {

    private Context context;
    private List<File> fileList;

    public WorkAdapter(Context context, List<File> fileList) {
        this.context = context;
        this.fileList = fileList;
    }

    @NonNull
    @Override
    public WorkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.item_work,parent,false);
        return new WorkViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkViewHolder holder, int position) {
        holder.imageView.setImageURI(Uri.fromFile(fileList.get(position)));
        holder.setImageOnClick(new ImageOnClick() {
            @Override
            public void onClick(int pos) {
                Intent intent=new Intent(context, ViewFileActivity.class);
                intent.setData(Uri.fromFile(fileList.get(pos)));
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }
}
