package me.xiaopan.android.spear.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import me.xiaopan.android.spear.process.ImageProcessor;

public class DrawableHolder {
    private int resId;	//当正在加载时显示的图片
    private boolean process;
    private BitmapDrawable drawable;	//当加载地址为空时显示的图片

    public void setResId(int resId) {
        if(this.resId != resId && drawable != null){
            if(!drawable.getBitmap().isRecycled()){
                drawable.getBitmap().recycle();
            }
            drawable = null;
        }
        this.resId = resId;
    }

    public boolean isProcess() {
        return process;
    }

    public void setProcess(boolean process) {
        if(this.process != process && drawable != null){
            if(!drawable.getBitmap().isRecycled()){
                drawable.getBitmap().recycle();
            }
            drawable = null;
        }
        this.process = process;
    }

    public BitmapDrawable getDrawable(Context context, ImageProcessor imageProcessor) {
        if(drawable == null && resId > 0){
            boolean finished = false;
            if(!process || imageProcessor == null){
                Drawable defaultDrawable = context.getResources().getDrawable(resId);
                if(defaultDrawable != null && defaultDrawable instanceof BitmapDrawable){
                    drawable = (BitmapDrawable) defaultDrawable;
                    finished = true;
                }
            }

            if(!finished){
                Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resId);
                if(bitmap != null){
                    if(process && imageProcessor != null){
                        Bitmap newBitmap = imageProcessor.process(bitmap, null, ImageView.ScaleType.CENTER_CROP);
                        if(newBitmap != bitmap){
                            bitmap.recycle();
                            bitmap = newBitmap;
                        }
                    }
                    drawable = new BitmapDrawable(context.getResources(), bitmap);
                }
            }
        }
        return drawable;
    }

    public void reset() {
        this.drawable = null;
    }
}
