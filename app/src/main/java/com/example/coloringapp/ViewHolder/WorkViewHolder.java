package com.example.coloringapp.ViewHolder;

import android.view.ContextMenu;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.coloringapp.Interface.ImageOnClick;
import com.example.coloringapp.R;
import com.example.coloringapp.common.Common;

public class WorkViewHolder extends RecyclerView.ViewHolder {

    public ImageView imageView;
    private ImageOnClick imageOnClick;

    public void setImageOnClick(ImageOnClick imageOnClick) {
        this.imageOnClick = imageOnClick;
    }

    public WorkViewHolder(@NonNull View itemView) {
        super(itemView);

        imageView=itemView.findViewById(R.id.image);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                imageOnClick.onClick(getAdapterPosition());
            }
        });
        itemView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                menu.add(0,0,getAdapterPosition(), Common.DELETE);
            }
        });
    }
}
