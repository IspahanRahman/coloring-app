package com.example.coloringapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.coloringapp.Interface.ImageOnClick;
import com.example.coloringapp.PaintActivity;
import com.example.coloringapp.R;
import com.example.coloringapp.ViewHolder.ImageViewHolder;
import com.example.coloringapp.WorkListActivity;
import com.example.coloringapp.common.Common;

import java.util.ArrayList;
import java.util.List;

public class ImageAdapter  extends RecyclerView.Adapter<ImageViewHolder> {
    private Context mContext;
    private List<Integer> listImages;

    public ImageAdapter(Context mContext) {
        this.mContext = mContext;
        this.listImages=getImages();
    }
    private List<Integer> getImages()
    {
        List<Integer> results=new ArrayList<>();

        results.add(R.drawable.outline_butterfly);
        results.add(R.drawable.outline_bird);
        results.add(R.drawable.outline_building);
        results.add(R.drawable.outline_dog);
        results.add(R.drawable.outline_fish);
        results.add(R.drawable.outline_mountain);
        results.add(R.drawable.outline_scenery);
        results.add(R.drawable.outline_sea);
        results.add(R.drawable.outline_shark);

        return results;


    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_images,parent,false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, final int position) {

        holder.imageView.setImageResource(listImages.get(position));
        holder.setImageOnClick(new ImageOnClick() {
            @Override
            public void onClick(int pos) {
                Common.ITEM_SELECTED=""+(position+1);
                Common.PICTURE_SELECTED=listImages.get(pos);
                mContext.startActivity(new Intent(mContext, PaintActivity.class));
            }
        });
        holder.imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Common.ITEM_SELECTED=""+(1+position);
                mContext.startActivity(new Intent(mContext, WorkListActivity.class));
            }
        });
    }

    @Override
    public int getItemCount() {
        return listImages.size();
    }
}
