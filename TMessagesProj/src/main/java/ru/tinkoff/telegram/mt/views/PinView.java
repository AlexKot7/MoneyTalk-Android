package ru.tinkoff.telegram.mt.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import ru.tinkoff.telegram.mt.ui.TgR;


/**
 * @author a.shishkin1
 */


public class PinView extends View {

    private static final int DEFAULT_DIGITS_COUNT = 4;


    private int padding;
    private int textPadding;
    private int radius;

    private int colorError;
    private int colorEmpty;
    private int colorDigit;

    private Paint paint;
    private Paint textPaint;

    private char[] current;
    private int index;

    private boolean isError;

    private Rect actualTextBounds;

    private String description;


    public PinView(Context context) {
        super(context);
        init();
    }

    public PinView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PinView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        setTextAlpha(1f);
        textPaint.setColor(0xFF9299A2);
        textPaint.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14, getResources().getDisplayMetrics()));
        padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics());
        textPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
        radius = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
        colorDigit = TgR.color.blue_color;
        colorError = 0xFFDD5656;
        colorEmpty = 0xff8a8a8a;
        current = new char[DEFAULT_DIGITS_COUNT];
        index = -1;
        actualTextBounds = new Rect();

    }

    public void markError() {
        try {
            ((Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE)).vibrate(600);
        } catch (Exception e) {
            // ignore
        }
        isError = true;
        invalidate();
        postDelayed(new Runnable() {
            @Override
            public void run() {
                isError = false;
                clear();
                invalidate();
            }
        }, 1200);
    }

    public void markError(final Runnable r) {
        try {
            ((Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE)).vibrate(600);
        } catch (Exception e) {
            // ignore
        }
        isError = true;
        invalidate();
        postDelayed(new Runnable() {
            @Override
            public void run() {
                isError = false;
                clear();
                invalidate();
                r.run();
            }
        }, 1200);
    }

    public void clear() {
        index = -1;
        invalidate();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int elementSize = radius * 2 + padding * 2;
        int wantHeight = getPaddingTop() + getPaddingBottom() + elementSize;

        int wantWidth = getPaddingLeft() + getPaddingRight() + elementSize * getMaxLength();

        int addHeight = 0;
        int textWidth = 0;
        actualTextBounds.set(0, 0, 0, 0);
        if (description != null) {
            String[] labelStrings = description.split("\n");
            for (int i = 0; i < labelStrings.length; i++) {
                String str = labelStrings[i];
                textPaint.getTextBounds(str, 0, str.length(), actualTextBounds);
                int currentTextWidth = actualTextBounds.width();
                if (currentTextWidth > textWidth) {
                    textWidth = currentTextWidth;
                }
                addHeight += (textPadding + actualTextBounds.height());
            }
        }

        wantHeight += addHeight > 0 ? addHeight + textPadding : 0;
        textWidth = getPaddingLeft() + textWidth + getPaddingRight();
        wantWidth = Math.max(wantWidth, textWidth);

        if (widthMode == MeasureSpec.UNSPECIFIED) {
            widthSize = wantWidth;
        } else {
            widthSize = Math.min(widthSize, wantWidth);
        }

        if (heightMode == MeasureSpec.UNSPECIFIED) {
            heightSize = wantHeight;
        } else {
            heightSize = Math.min(heightSize, wantHeight);
        }

        setMeasuredDimension(widthSize, heightSize);
    }

    public int getMaxLength() {
        return current.length;
    }

    public boolean isFill() {
        return index >= getMaxLength() - 1;
    }

    public boolean isEmpty() {
        return index < 0;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        actualTextBounds.set(0, 0, 0, 0);
        int elementsXOffset = 0;
        int elementsYOffset = 0;
        if (description != null) {
            String[] labelStrings = description.split("\n");


            int elementWidth = 2 * padding + 2 * radius;
            int allElementsWidth = elementWidth * getMaxLength();
            int h = 0;
            for (int i = 0; i < labelStrings.length; i++) {
                String str = labelStrings[i];
                textPaint.getTextBounds(str, 0, str.length(), actualTextBounds);
                int textWidth = actualTextBounds.width();
                int y = getPaddingTop() - (int) textPaint.getFontMetrics().ascent + (i * textPadding) + h;
                h += actualTextBounds.height();
                int halfDiff = (textWidth - allElementsWidth) / 2;
                int x = getPaddingLeft();
                int textXOffset = 0;
                if (halfDiff < 0) {
                    textXOffset = -halfDiff;
                }
                x += textXOffset;
                canvas.drawText(str, x, y, textPaint);
                if (halfDiff > 0 && elementsXOffset < halfDiff) {
                    elementsXOffset = halfDiff;
                }
                elementsYOffset += textPadding + actualTextBounds.height();
            }

        }

        int x = getPaddingLeft() + elementsXOffset;
        int y = getPaddingTop() + elementsYOffset;
        int half = padding + radius;
        int width = half * 2;
        for (int i = 0; i < getMaxLength(); i++) {
            paint.setColor(colorByIndexAndState(i));
            canvas.drawCircle(x + half, y + half, radius, paint);
            x += width;
        }


    }

    private int colorByIndexAndState(int i) {
        return isError ? colorError : i <= index ? colorDigit : colorEmpty;
    }

    public void addChar(char c) {
        if (isError)
            return;
        index++;
        if (index > getMaxLength() - 1) {
            index = getMaxLength() - 1;
            return;
        }
        current[index] = c;
        invalidate();
    }

    public void removeLast() {
        if (isError)
            return;
        index--;
        if (index < -1)
            index = -1;
        invalidate();
    }


    public String getValue() {
        if (index < 0 || index >= getMaxLength())
            return null;
        char[] chars = new char[index + 1];
        System.arraycopy(current, 0, chars, 0, index + 1);
        return new String(chars);
    }


    public void setDescription(String description) {
        this.description = description;
        requestLayout();
    }

    public void setTextAlpha(float textAlpha) {

        textPaint.setAlpha((int) ((textAlpha) * 255));
        invalidate();
    }
}
