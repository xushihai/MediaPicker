package com.hai.mediapicker.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

/**
 * 作者：徐仕海
 * 公司：德宁普科技有限公司
 * Created on 2016/9/1 0001.
 */
public class SquareImageView extends AppCompatImageView {
    Drawable shade;
    boolean showShade = false;

    public SquareImageView(Context context) {
        super(context);
    }

    public SquareImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (showShade && shade != null) {
            shade.setBounds(0, 0, getWidth(), getHeight());
            shade.draw(canvas);
        }
    }

    public boolean isShowShade() {
        return showShade;
    }

    public Drawable getShade() {
        return shade;
    }

    public void setShade(Drawable shade) {
        this.shade = shade;
    }

    public void setShowShade(boolean showShade) {
        this.showShade = showShade;
        invalidate();
    }

    public void justSetShowShade(boolean showShade) {
        this.showShade = showShade;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }
}
