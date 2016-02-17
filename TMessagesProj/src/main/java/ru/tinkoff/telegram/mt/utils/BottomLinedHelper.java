package ru.tinkoff.telegram.mt.utils;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

/**
 * @author a.shishkin1
 */


public class BottomLinedHelper {


    private Paint paint = new Paint();


    public BottomLinedHelper() {
        paint.setColor(0xffd9d9d9);
        paint.setStrokeWidth(1);
    }


    public void onDraw(View view, Canvas canvas) {
        int y = view.getHeight() - 1;
        canvas.drawLine(0, y, view.getWidth(), y, paint);
    }


}
