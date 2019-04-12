package com.hai.mediapicker.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.hai.mediapicker.R;

/**
 * Created by Administrator on 2017/6/14.
 */

public class RingProgress extends View{

    int max,progress;
    int finishColor,backgroundColor;
    int stokeWidth;

    public static final int START_FROM_TOP=-90;
    public static final int START_FROM_RIGHT=0;
    public static final int START_FROM_BOTTOM=90;
    public static final int START_FROM_LEFT=180;
    int startAngle=START_FROM_TOP;

    Paint paint,bgPaint;
    public RingProgress(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        TypedArray typedArray = context.obtainStyledAttributes(attrs,R.styleable.RingProgress);

        max = typedArray.getInteger(R.styleable.RingProgress_max,100);
        progress=typedArray.getInteger(R.styleable.RingProgress_progress,0);
        finishColor= typedArray.getColor(R.styleable.RingProgress_progress_color,Color.GREEN);
        backgroundColor=typedArray.getColor(R.styleable.RingProgress_background_color,Color.WHITE);
        stokeWidth = typedArray.getDimensionPixelSize(R.styleable.RingProgress_ring_width,10);
        startAngle = typedArray.getInteger(R.styleable.RingProgress_start_angle,START_FROM_TOP);

        typedArray.recycle();


        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(finishColor);
        paint.setStyle(Paint.Style.FILL);

        bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(backgroundColor);
        bgPaint.setStyle(Paint.Style.FILL);
    }



    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        RectF rectF=new RectF(0,0,getWidth(),getHeight());

        canvas.drawArc(rectF,startAngle,360*progress*1.0f/max,true,paint);
//        canvas.drawArc(rectF,startAngle,360*progress*1.0f/max+startAngle,true,paint);
        canvas.drawCircle(getWidth()/2,getHeight()/2,getHeight()/2-stokeWidth,bgPaint);
    }

    public int getStartAngle() {
        return startAngle;
    }

    public void setStartAngle(int startAngle) {
        this.startAngle = startAngle;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
        invalidate();
    }

    public int getFinishColor() {
        return finishColor;
    }

    public void setFinishColor(int finishColor) {
        this.finishColor = finishColor;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    @Override
    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public int getStokeWidth() {
        return stokeWidth;
    }

    public void setStokeWidth(int stokeWidth) {
        this.stokeWidth = stokeWidth;
    }
}
