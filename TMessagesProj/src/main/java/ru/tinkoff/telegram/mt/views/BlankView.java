package ru.tinkoff.telegram.mt.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

/**
 * @author a.shishkin1
 */


public class BlankView extends View {


    public BlankView(Context context) {
        super(context);
    }

    private Paint paint = new Paint();
    private Rect r = new Rect();
    private String text = "";

    {
        paint.setTextSize(59);
        paint.setColor(Color.GREEN);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        paint.getTextBounds(text, 0, text.length(), r);
        canvas.drawText(text, (getWidth() - r.width()) >> 1, (getHeight() - r.height()) >> 1, paint);
    }

    public BlankView withText(String text) {
        this.text = text;
        return this;
    }


}
