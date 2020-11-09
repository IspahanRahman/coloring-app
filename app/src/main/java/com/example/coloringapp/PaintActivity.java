package com.example.coloringapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.coloringapp.Widget.PaintSurfaceView;
import com.example.coloringapp.Widget.PaintView;
import com.example.coloringapp.common.Common;
import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.thebluealliance.spectrum.SpectrumPalette;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class PaintActivity extends AppCompatActivity implements SpectrumPalette.OnColorSelectedListener {
    private static final int PERMISSION_REQUEST =1001 ;
    PaintSurfaceView paintView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paint);

        initToolbar();
        SpectrumPalette spectrumPalette=findViewById(R.id.palette);
        spectrumPalette.setOnColorSelectedListener(this);
        paintView=findViewById(R.id.pain_view);
    }

    @Override
    protected void onResume() {
        super.onResume();
        paintView.startDrawThread();
    }

    @Override
    protected void onPause() {
        paintView.stopDrawThread();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Common.IMAGE_FROM_GALLERY=null;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void initToolbar() {
        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.close);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id=item.getItemId();
        if(id==R.id.action_save){
            ShowDialogForSave();
        }else if(id==android.R.id.home)
        {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void ShowDialogForSave() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},PERMISSION_REQUEST);
            }
        }
        else{
            AlertDialog.Builder builder=new AlertDialog.Builder(this);
            builder.setTitle("save picture?");

            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            builder.setPositiveButton("yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        save();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    dialog.dismiss();
                }
            });

            builder.show();

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==PERMISSION_REQUEST && grantResults.length >0&& grantResults[0]==PackageManager.PERMISSION_GRANTED)
        {
            try {
                save();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void save() throws IOException {
        Bitmap bitmap=paintView.getBitmap();
        String file_name= UUID.randomUUID()+".png";
        OutputStream outputStream;
        boolean saved;
        File folder;
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q) {
            folder = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES) + File.separator + getString(R.string.app_name));
        }
        else {
            folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + getString(R.string.app_name));
        }
        if(!folder.exists())
        {
            folder.mkdirs();
        }
        File subFolder=new File(folder,Common.ITEM_SELECTED);
        if(!subFolder.exists())
        {
            subFolder.mkdirs();
        }

            File image=new File(subFolder+File.separator+file_name);
        Uri imageUri=Uri.fromFile(image);

        outputStream=new FileOutputStream(image);
            saved=bitmap.compress(Bitmap.CompressFormat.PNG,100,outputStream);

if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q)
{
    ContentResolver resolver=getContentResolver();
    ContentValues contentValues=new ContentValues();
    contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME,file_name);
    contentValues.put(MediaStore.MediaColumns.MIME_TYPE,"image/png");
    contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH,Environment.DIRECTORY_PICTURES+File.separator+getString(R.string.app_name));
    Uri uri=resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);
    outputStream=resolver.openOutputStream(uri);
    saved=bitmap.compress(Bitmap.CompressFormat.PNG,100,outputStream);

}
else {
    sendPictureToGallery(imageUri);

}
if(saved){
        Toast.makeText( this,"picture saved",Toast.LENGTH_SHORT).show();
}else {

    Toast.makeText( this,"picture not saved",Toast.LENGTH_SHORT).show();
}
outputStream.flush();
outputStream.close();
        finish();
    }

    private void sendPictureToGallery(Uri imageUri) {
        Intent intent=new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(imageUri);
        sendBroadcast(intent);
    }

    @Override
    public void onColorSelected(int color) {
        Common.COLOR_SELECTED=color;
    }

    public void selectColor(View view) {
        ColorPickerDialogBuilder.with(this)
                .initialColor(Common.COLOR_SELECTED)
                .setTitle("Select Color")
                .density(12)
                .wheelType(ColorPickerView.WHEEL_TYPE.CIRCLE)
                .setPositiveButton("Ok", new ColorPickerClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int lastSelectedColor, Integer[] allColors) {
                        Common.COLOR_SELECTED=lastSelectedColor;
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).build()
                .show();
    }

    public void undoLastAction(View view) {
        ImageButton button=(ImageButton)view;

        button.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                paintView.newPage();
                return true;
            }
        });
        paintView.undoLastAction();
    }
}