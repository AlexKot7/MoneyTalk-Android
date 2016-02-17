package ru.tinkoff.telegram.mt.views;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import ru.tinkoff.telegram.mt.utils.BottomLinedHelper;

/**
 * @author a.shishkin1
 */


public class BottomLinedLinearLayout extends LinearLayout {

    private BottomLinedHelper mBottomLinedHelper = new BottomLinedHelper();

    public BottomLinedLinearLayout(Context context) {
        super(context);
    }

    public BottomLinedLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BottomLinedLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        mBottomLinedHelper.onDraw(this, canvas);
        super.dispatchDraw(canvas);
    }
}
