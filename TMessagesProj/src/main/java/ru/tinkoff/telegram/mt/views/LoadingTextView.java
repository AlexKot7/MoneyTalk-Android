package ru.tinkoff.telegram.mt.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

/**
 * @author a.shishkin1
 */


public class LoadingTextView extends View {

    public static final int READY = 0;
    public static final int LOADING = 1;

//    private Rect tmpRect = new Rect();
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int mode;
    private int step = 0;
    private String hint = "";
    private FontMetricsInt fm;

    private int baseLine;

    public void setHint(String hint) {
        this.hint = hint;
        requestLayout();
        invalidate();
    }

    public LoadingTextView(Context context) {
        super(context);
        init();
    }

    public LoadingTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LoadingTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init()  {
        mode = LOADING;
        paint.setColor(0xff8a8a8a);
        paint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getContext().getResources().getDisplayMetrics()));
        fm = paint.getFontMetricsInt();
    }

    private String text;
    private String dots = "...";

    public void setText(String text) {
        this.text = text;
        requestLayout();
        invalidate();
    }

    public void setMode(int mode) {
        this.mode = mode;
        requestLayout();
        invalidate();
        if(mode == LOADING) {
            startLoadAnimation();
        }
    }



    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        baseLine = - fm.top;

        int h = baseLine + fm.bottom;
        int w = 0;

        if (text != null) {
            w += (int) paint.measureText(text);
        }

        w = Math.max((int) paint.measureText(hint), w);

        if(mode == LOADING) {
            w += (int) paint.measureText(dots);
        }

        if (widthMode == MeasureSpec.UNSPECIFIED) {
            widthSize = w;
        } else if(widthMode == MeasureSpec.AT_MOST) {
            widthSize = Math.min(w, widthSize);
        }

        if (heightMode == MeasureSpec.UNSPECIFIED) {
            heightSize = h;
        } else if(heightMode == MeasureSpec.AT_MOST) {
            heightSize = Math.min(h, heightSize);
        }

        setMeasuredDimension(widthSize, heightSize);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        int xOffset = 0;
        if (mode == LOADING) {
            canvas.drawText(hint, 0, baseLine, paint);
            xOffset = (int) paint.measureText(hint);
            String curDots = dots.substring(0, step);
            canvas.drawText(curDots, xOffset, baseLine, paint);
        } else {
            if (text != null) {
                canvas.drawText(text, 0, baseLine, paint);
            }
        }
    }

    private Runnable update = new Runnable() {
        @Override
        public void run() {
            setStep(step + 1);
            postDelayed(this, 300);
        }
    };

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if(mode == LOADING && getVisibility() == VISIBLE) {
            startLoadAnimation();
        }
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if(mode == LOADING && getVisibility() == VISIBLE) {
            startLoadAnimation();
        } else {
            removeCallbacks(update);
        }
    }

    private void startLoadAnimation() {
        removeCallbacks(update);
        postDelayed(update, 300);
    }

    @Override
    protected void onDetachedFromWindow() {
        removeCallbacks(update);
        super.onDetachedFromWindow();
    }




    private void setStep(int step) {
        this.step = step % 4;
        requestLayout();
        invalidate();
    }

}
