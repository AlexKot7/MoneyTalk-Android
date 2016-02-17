package ru.tinkoff.telegram.mt.views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;

import ru.tinkoff.telegram.mt.ui.TgR;

/**
 * @author a.shishkin1
 */


public class PagesView extends View {

    private Rect source;
    private Rect tmp;
    private int space;


    private int value;
    private int count;
    private int realSpace;
    private Paint paint = new Paint();
    private Paint paintMarked = new Paint();

    public PagesView(Context context) {
        super(context);
        init();
    }

    public PagesView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PagesView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        int rectSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, dm);
        source = new Rect(0, 0, rectSize, rectSize);
        tmp = new Rect(0, 0, rectSize, rectSize);
        space = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 7, dm);
        paint.setColor(0xffbbbbbb);
        paintMarked.setColor(TgR.color.blue_color);
        setCount(3);
        setValue(1);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);


        if(widthMode == MeasureSpec.AT_MOST) {
            widthSize = Math.min(wantWidth(), widthSize);
        } else if (widthMode == MeasureSpec.UNSPECIFIED) {
            widthSize = wantHeight();
        }

        if(heightMode == MeasureSpec.AT_MOST) {
            heightSize = Math.min(wantHeight(), heightSize);
        } else if (heightMode == MeasureSpec.UNSPECIFIED) {
            heightSize = wantHeight();
        }

        setMeasuredDimension(widthSize, heightSize);
    }

    private int wantHeight() {
        return source.height();
    }

    private int wantWidth() {
        return source.width() * count + space * (count + 1);
    }

    public void setValue(int value) {
        this.value = value;
        invalidate();
    }

    public void setCount(int count) {
        this.count = count;
        requestLayout();
        invalidate();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        int width = right - left;
        realSpace = (width - count * source.width()) / (count + 1);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int offset = realSpace + source.width();

        tmp.set(source);
        for(int i = 0; i < count; i++) {
            canvas.drawRect(tmp, i == (value - 1) ? paintMarked : paint);
            tmp.offset(offset, 0);
        }
    }



}
