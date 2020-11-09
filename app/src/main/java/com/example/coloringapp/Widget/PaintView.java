package com.example.coloringapp.Widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import androidx.annotation.Nullable;

import com.example.coloringapp.common.Common;
import com.example.coloringapp.utils.FloodFill;

import java.util.ArrayList;
import java.util.List;

public class PaintView extends View {
    Bitmap bitmap;
    private float mPositionX,mPositionY;
    private float refX,refY;
    private ScaleGestureDetector mScaleGestureDetector;
    private float mScaleFactor=1.0f;
    private final static float mMinZoom=1.0f;
    private final static float mMaxZoom=5.0f;
    private List<Bitmap> bitmapList=new ArrayList<>();

    private Bitmap defaultBitmap=null;

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

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener{
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor*=detector.getScaleFactor();
            mScaleFactor=Math.max(mMinZoom,Math.min(mScaleFactor,mMaxZoom));
            invalidate();
            
            return true;

        }
    }
    public PaintView(Context context) {
        super(context);
    }

    public PaintView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

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

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBitmap(canvas);
    }

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
        if(targetColor!=Color.BLACK) {
            FloodFill.floodFill(bitmap, new Point(x, y), targetColor, Common.COLOR_SELECTED);
            addLastAction(Bitmap.createBitmap(getBitmap()));
            invalidate();
        }
    }

    public Bitmap getBitmap() {
        return bitmap;
    }
}
