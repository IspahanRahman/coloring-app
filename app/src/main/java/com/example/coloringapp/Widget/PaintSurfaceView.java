package com.example.coloringapp.Widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.coloringapp.common.Common;
import com.example.coloringapp.utils.FloodFill;

import java.util.ArrayList;
import java.util.List;

public class PaintSurfaceView extends SurfaceView implements SurfaceHolder.Callback,Runnable{
    Bitmap bitmap;
    private float mPositionX,mPositionY;
    private float refX,refY;
    private ScaleGestureDetector mScaleGestureDetector;
    private float mScaleFactor=1.0f;
    private final static float mMinZoom=1.0f;
    private final static float mMaxZoom=5.0f;
    private List<Bitmap> bitmapList=new ArrayList<>();

    private Bitmap defaultBitmap=null;
    private static final int MAX_BITMAP=10;
    private SurfaceHolder holder;
    private Thread drawThread;
    private boolean surfaceReady=false;
    private boolean drawingActive=false;
    private static final int MAX_FRAME_TIME=(int)(1000.0/60.0);
    private static final String LOGTAG="Surface view";

    public void undoLastAction() {
        if(bitmapList.size()>0)
        {
            bitmapList.remove(bitmapList.size()-1);
            if(bitmapList.size()>0)
            {
                bitmap=bitmapList.get(bitmapList.size()-1);
            }
            else{
                bitmap=Bitmap.createBitmap(defaultBitmap);
            }
            invalidate();
        }
    }
    private void addLastAction(Bitmap b)
    {
        bitmapList.add(b);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        this.holder=holder;
        if(drawThread!=null)
        {
            drawingActive=false;
            try {
                drawThread.join();
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }
        surfaceReady=true;
        startDrawThread();

    }

    public void startDrawThread() {
        if(surfaceReady && drawThread==null)
        {
            drawThread=new Thread(this,"Draw thread");
            drawingActive=true;
            drawThread.start();
        }
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        if(width==0 || height==0)
        {
            return;
        }

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

        stopDrawThread();
        holder.getSurface().release();
        this.holder=null;
        surfaceReady=false;
    }

    public void stopDrawThread() {
        if (drawThread==null)
        {
            Log.d(LOGTAG,"Request last frame");
            try {
                Log.d(LOGTAG,"Request last frame");
                drawThread.join(5000);

            } catch (InterruptedException e) {
               Log.e(LOGTAG,"Could not join with draw thread");
            }

        }
        drawThread=null;
    }

    @Override
    public void run() {
        Log.d(LOGTAG,"Draw thread started");
        long frameStartTime;
        long frameTime;
        if(Build.BRAND.equalsIgnoreCase("google")&& Build.MANUFACTURER.equalsIgnoreCase("acer")&& Build.MODEL.equalsIgnoreCase("Pixel 2")){
            Log.w(LOGTAG,"sleep 500ms (Device : Acer Pixel 2)");
            try {
                Thread.sleep(5000);
            }catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }

        try{
            while(drawingActive)
            {
                if(holder==null)
                {
                    return;
                }
                frameStartTime=System.nanoTime();
                Canvas canvas=holder.lockCanvas();
                if(canvas!=null)
                {
                    try {
                        canvas.drawColor(Color.WHITE);
                        drawBitmap(canvas);

                    }finally {
                        holder.unlockCanvasAndPost(canvas);
                    }

                }
                frameTime=(System.nanoTime()-frameStartTime)/1000000;

                if(frameTime<MAX_FRAME_TIME)
                {
                    try {
                        Thread.sleep(MAX_FRAME_TIME - frameStartTime);
                    }catch (Exception e){}
                }
            }
        }
        catch (Exception e)
        {
            Log.w(LOGTAG,"Exception while locking/unlocking");
        }
        Log.d(LOGTAG,"Draw thread finished");

    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener{
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor*=detector.getScaleFactor();
            mScaleFactor=Math.max(mMinZoom,Math.min(mScaleFactor,mMaxZoom));
            invalidate();

            return true;

        }
    }
    public PaintSurfaceView(Context context) {
        super(context);
    }

    public PaintSurfaceView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        holder=getHolder();
        holder.addCallback(this);

        mScaleGestureDetector=new ScaleGestureDetector(context,new ScaleListener());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if(bitmap==null) {
            Bitmap srcBitmap =null;
            if(Common.IMAGE_FROM_GALLERY!=null){
            srcBitmap=Common.IMAGE_FROM_GALLERY;

            }else {
                srcBitmap = BitmapFactory.decodeResource(getResources(), Common.PICTURE_SELECTED);
            } bitmap = Bitmap.createScaledBitmap(srcBitmap, w, h, false);

            for(int i=0;i<bitmap.getWidth();i++)
            {
                for(int j=0;j<bitmap.getHeight();j++){
                    int alpha=255-brightness(bitmap.getPixel(i,j));
                    if(alpha<200)
                    {
                        bitmap.setPixel(i,j,Color.WHITE);

                    }else{
                        bitmap.setPixel(i,j,Color.BLACK);

                    }
                }
            }
            if(defaultBitmap==null)
            {
                defaultBitmap=Bitmap.createBitmap(bitmap);
            }
        }
    }

    private int brightness(int pixel) {
        return (pixel >>16) & 0xff;
    }
/*
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBitmap(canvas);
    }

 */

    private void drawBitmap(Canvas canvas) {
        canvas.save();
        canvas.translate(mPositionX,mPositionY);
        canvas.scale(mScaleFactor,mScaleFactor);
        canvas.drawBitmap(bitmap,0,0,null);
        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mScaleGestureDetector.onTouchEvent(event);
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                refX=event.getX();
                refY=event.getY();
                paint((int)((refX-mPositionX)/mScaleFactor),(int)((refY-mPositionY)/mScaleFactor));
                break;

            case MotionEvent.ACTION_MOVE:
                float nX=event.getX();
                float nY=event.getY();

             mPositionX+=nX-refX;
             mPositionY+=nY-refY;

             invalidate();

        }
        return true;
    }

    private void paint(int x, int y) {
        if(x<0 || y<0 || x>=bitmap.getWidth() || y>=bitmap.getHeight())
            return;
        int targetColor=bitmap.getPixel(x,y);
        if(targetColor!=Color.BLACK && targetColor!=Common.COLOR_SELECTED) {
            FloodFill.floodFill(bitmap, new Point(x, y), targetColor, Common.COLOR_SELECTED);
            addLastAction(Bitmap.createBitmap(getBitmap()));
            if(bitmapList.size()>MAX_BITMAP)
            {
                for(int i=0;i<5;i++)
                {
                    bitmapList.remove(i);
                }
            }
            invalidate();
        }
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void newPage()
    {
        bitmap=Bitmap.createBitmap(defaultBitmap);
        invalidate();
    }
}
