package ru.tinkoff.telegram.mt.views;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.EditText;

/**
 * @author a.shishkin1
 */


public class CardNumberEditText extends EditText {


    public static final int FULL_MODE = 0;
    public static final int SHORT_MODE = 1;

    private int charsCount = 4;
    private int mode;
    private String realText;
    private float animationFactor = 0f;

    public CardNumberEditText(Context context) {
        super(context);
    }

    public CardNumberEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CardNumberEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public String getRealText() {
        if(mode == SHORT_MODE) {
            return realText;
        }
        return getText().toString();
    }

    public void setMode(int mode) {
        this.mode = mode;
        if(mode == FULL_MODE) {
            setText(realText);
        } else if(mode == SHORT_MODE) {
            realText = getText().toString();
            int l = realText.length();
            setText(realText.substring(Math.max(0, l - charsCount), l));
        }
        animationFactor = 0f;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        String text = getText().toString();
        boolean isSpecificDrawActual = text.length() > 4 && mode == FULL_MODE;

        if(isSpecificDrawActual) {
            int l = text.length();
            float dist = getPaint().measureText(text.substring(0, Math.max(0, l - charsCount)));
            canvas.save();
            canvas.translate(-dist * animationFactor, 0);
        }
        super.onDraw(canvas);
        if(isSpecificDrawActual) {
            canvas.restore();
        }
    }

    public void setAnimationFactor(float animationFactor) {
        this.animationFactor = animationFactor;
        invalidate();
    }

    public int getMode() {
        return mode;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(mode == SHORT_MODE)
            return false;
        return super.onTouchEvent(event);
    }
}
