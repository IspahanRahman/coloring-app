package com.example.coloringapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.coloringapp.adapters.ImageAdapter;
import com.example.coloringapp.common.Common;

import java.io.FileNotFoundException;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_PERMISSION =1001 ;
    private static final int PICK_IMAGE = 1111;
    RecyclerView recyclerView;
ImageAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initToolbar();

        initView();
    }

    private void initView() {
        recyclerView=findViewById(R.id.recycler_view_images);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter=new ImageAdapter(this);
        recyclerView.setAdapter(adapter);
    }

    private void initToolbar() {
        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        int toolbar_item_color= ResourcesCompat.getColor(getResources(),R.color.blue,null);
        toolbar.setTitleTextColor(toolbar_item_color);
        toolbar.setSubtitleTextColor(toolbar_item_color);
        getSupportActionBar().setTitle("My Pictures");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.close);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id=item.getItemId();
        if(id==android.R.id.home)
        {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void getImageFromGallery(View view) {
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_PERMISSION);
            }
        }
        else
        {
            getImage();
        }
    }

    private void getImage() {
        Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent,"Select Picture"),PICK_IMAGE);

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==REQUEST_PERMISSION && grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
        {
            getImage();
        }
        else{
            Toast.makeText(this,"you need accept permission",Toast.LENGTH_SHORT).show();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode==PICK_IMAGE && data!=null && resultCode==RESULT_OK)
        {
            Bitmap bitmap=null;
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.KITKAT){
                ParcelFileDescriptor fileDescriptor= null;
                try {
                    fileDescriptor = getContentResolver().openFileDescriptor(data.getData(),"r");
                    bitmap= BitmapFactory.decodeFileDescriptor(fileDescriptor.getFileDescriptor());

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
            else {
                Uri pickedImage=data.getData();
                String[] filePath={MediaStore.Images.Media.DATA};
                Cursor cursor=getContentResolver().query(pickedImage,filePath,null,null,null);
                cursor.moveToFirst();
                String imgPath=cursor.getString(cursor.getColumnIndex(filePath[0]));

                BitmapFactory.Options options=new BitmapFactory.Options();
                options.inPreferredConfig=Bitmap.Config.ARGB_8888;
                bitmap=BitmapFactory.decodeFile(imgPath,options);
                cursor.close();

            }
            Common.IMAGE_FROM_GALLERY=bitmap;
            Common.ITEM_SELECTED="0";
            startActivity(new Intent(this,PaintActivity.class));

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void showImages(View view) {
        Common.ITEM_SELECTED="0";
        startActivity(new Intent(this,WorkListActivity.class));
    }
}