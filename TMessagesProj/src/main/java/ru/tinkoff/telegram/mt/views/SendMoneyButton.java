package ru.tinkoff.telegram.mt.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import ru.tinkoff.telegram.mt.R;

/**
 * @author a.shishkin1
 */


public class SendMoneyButton extends View {

    private static final int HORIZONTAL_PADDING_PX = 16;
    private static final float TEXT_SIZE_PX = 16.f;

    private static final int COLOR_USUAL = 0xFFEDAE0D;
    private static final int COLOR_PRESSED = 0xFFD39A09;

    private String text;
    private Bitmap icon;
    private RectF tmp;
    private Paint bgPaint;
    private Paint textPaint;

    private int horizontalPadding;

    public SendMoneyButton(Context context) {
        super(context);
        init();
    }

    public SendMoneyButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SendMoneyButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 53, getResources().getDisplayMetrics());
        int hms = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, hms);
    }

    private void init() {
        horizontalPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                HORIZONTAL_PADDING_PX,
                getResources().getDisplayMetrics());

        icon = BitmapFactory.decodeResource(getResources(), R.drawable.mt_ruble);
        text = getContext().getString(R.string.mt_send);
        bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);

        int textSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                TEXT_SIZE_PX,
                getResources().getDisplayMetrics());
        textPaint.setTextSize(textSize);

        tmp = new RectF();

        setClickable(true);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
        invalidate();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        final int bgColor = isPressed() ? COLOR_PRESSED : COLOR_USUAL;
        bgPaint.setColor(bgColor);

        int hh = getHeight() >> 1;
        tmp.set(0, 0, getWidth(), getHeight());
        canvas.drawRoundRect(tmp, hh, hh, bgPaint);

        if (icon != null) {
            canvas.drawBitmap(icon, horizontalPadding,
                    hh - (icon.getHeight() >> 1), null);
        }

        final int textWidth = (int) textPaint.measureText(text, 0, text.length());
        final int textX = (getWidth() - textWidth) >> 1;
        final Paint.FontMetricsInt fm = textPaint.getFontMetricsInt();
        final int textY = hh + fm.descent;
        canvas.drawText(text, textX, textY, textPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean result = super.onTouchEvent(event);
        invalidate();
        return result;
    }

}
