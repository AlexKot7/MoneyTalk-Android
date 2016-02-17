package ru.tinkoff.telegram.mt.views;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;


import org.telegram.messenger.AndroidUtilities;


import ru.tinkoff.telegram.mt.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author a.shishkin1
 */


public class CustomDigitKeyBoard extends View {


    private static final int DEFAULT_ANIMATION_TIME = 200;

    private int comfortButtonHeight;
    private int comfortButtonWidth;
    private PressAnimation animation;
    private Bitmap del;

    private static final int CODE_CANCEL = 103;
    private static final int CODE_CORRECT = 104;

    private Key[] keys;
    private Paint themePaint;

    private int fontSize;
    private int digitSize;
    private int additionalTopOffset;

    private KeyEventListener keyEventListener;


    public CustomDigitKeyBoard(Context context) {
        super(context);
        init();
    }

    public CustomDigitKeyBoard(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomDigitKeyBoard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        comfortButtonHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, AndroidUtilities.isTablet() ? 90 : 60, getResources().getDisplayMetrics());
        comfortButtonWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, AndroidUtilities.isTablet() ? 135 : 90, getResources().getDisplayMetrics());
        Point p = new Point();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getSize(p);
        int height = p.y;
        if(p.y < 540) {
            comfortButtonHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, getResources().getDisplayMetrics());
        }

        animation = new PressAnimation();
        themePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        themePaint.setColor(0xff8a8a8a);
        themePaint.setTextAlign(Paint.Align.CENTER);
        additionalTopOffset = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics());
        Typeface typeface = Typeface.create("sans-serif-light", Typeface.NORMAL);
        if(typeface != null) {
            themePaint.setTypeface(typeface);
        }
        fontSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14, getResources().getDisplayMetrics());
        digitSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 35, getResources().getDisplayMetrics());

        keys = new Key[]{
                new Key(1, '1'), new Key(2, '2'), new Key(3, '3'),
                new Key(4, '4'), new Key(5, '5'), new Key(6, '6'),
                new Key(7, '7'), new Key(8, '8'), new Key(9, '9'),
                new Key(CODE_CANCEL, null), new Key(0, '0'), new Key(CODE_CORRECT, null),
        };
        pressedPaint.setColor(0xDDDFE0);
        del = BitmapFactory.decodeResource(getResources(), R.drawable.mt_del);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int wantHeight = comfortButtonHeight * 4 + additionalTopOffset;
        int wantWidth = comfortButtonWidth * 3;

        if (heightMode == MeasureSpec.UNSPECIFIED) {
            heightSize = wantHeight;
        } else {
            heightSize = Math.min(heightSize, wantHeight);
        }

        if (widthMode == MeasureSpec.UNSPECIFIED) {
            widthSize = wantWidth;
        } else {
            widthSize = Math.min(widthSize, wantWidth);
        }

        // TODO move to onLayout
        int elementHeight = comfortButtonHeight;
        int elementWidth = (widthSize - getPaddingLeft() - getPaddingRight()) / 3;
        for (int i = 0; i < keys.length; i++) {
            int x = (i % 3) * elementWidth + getPaddingLeft();
            int y = (i / 3) * elementHeight;
            Key current = keys[i];
            current.r.set(x, y + additionalTopOffset, x + elementWidth, y + elementHeight + additionalTopOffset);
        }

        setMeasuredDimension(widthSize, heightSize);
    }



    private Rect meter = new Rect();
    private Rect pressedRect = new Rect();

    private Paint pressedPaint = new Paint();

    @Override
    protected void onDraw(Canvas canvas) {
        themePaint.setStrokeWidth(2);
        int xStart = getPaddingLeft();
        int xEnd = getWidth() - getPaddingRight();
        canvas.drawLine(xStart, 0, xEnd, 0, themePaint);

        for (Key current : keys) {
            Rect currentRect = current.r;


            AnimationFactor af = animation.get(current);
            if (current.state == Key.PRESSED || af != null) {
                float factor = af == null ? 1f : af.value;
                factor = factor * factor;
                int pressedAlpha = 225 - (int) (60 * (1 - factor));
                int defXOffset = (int) (currentRect.width() * 0.05f);
                int defYOffset = (int) (currentRect.height() * 0.05f);
                int pressedXOffset = (int) (defXOffset * (1 - factor) / 2);
                int pressedYOffset = (int) (defYOffset * (1 - factor) / 2);

                pressedRect.set(currentRect.left + pressedXOffset, currentRect.top + pressedYOffset, currentRect.right - pressedXOffset, currentRect.bottom - pressedYOffset);
                pressedPaint.setAlpha(pressedAlpha);
                canvas.drawRect(pressedRect, pressedPaint);

            }

            if (current.code != CODE_CORRECT) {
                String text = getStringByKey(current);
                themePaint.setTextSize(current.code == CODE_CANCEL ? fontSize : digitSize);
                themePaint.getTextBounds(text, 0, text.length(), meter);
                int xTextOffset = (currentRect.width() / 2);
                int yTextOffset = (currentRect.height() - ((int) themePaint.getFontMetrics().ascent + (int) themePaint.getFontMetrics().descent)) / 2;
                canvas.drawText(text, currentRect.left + xTextOffset, currentRect.top + yTextOffset, themePaint);
            } else {
                int xOffset = (currentRect.width() - del.getWidth()) / 2;
                int yOffset = (currentRect.height() - del.getHeight()) / 2;
                canvas.drawBitmap(del, currentRect.left + xOffset, currentRect.top + yOffset, themePaint);
            }

        }

    }

    public void setKeyEventListener(KeyEventListener keyEventListener) {
        this.keyEventListener = keyEventListener;
    }

    private Map<Key, String> names = new HashMap<>();

    private String getStringByKey(Key k) {
        String name = names.get(k);
        if (name == null) {
            switch (k.code) {
                case CODE_CORRECT:
                    name = "<";
                    break;
                case CODE_CANCEL:
                    name = getContext().getString(R.string.mt_cancel);
                    break;
                default:
                    name = new String(new char[]{k.content});
            }
            names.put(k, name);
        }
        return name;
    }

    private static class Key {

        private static final int PRESSED = 1;
        private static final int DEFAULT = 0;

        private Rect r;
        private int code;
        private Character content;
        private int state;


        private Key(int code, Character content) {
            this.code = code;
            this.content = content;
            this.state = DEFAULT;
            this.r = new Rect();
        }

        @Override
        public String toString() {
            return String.format("[ %d : %d ]", code, state);
        }
    }

    private Key pressed;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled())
            return false;
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            resolvePress((int) event.getX(), (int) event.getY());
            return true;
        }

        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            resolvePress((int) event.getX(), (int) event.getY());
        }

        if (event.getAction() == MotionEvent.ACTION_UP) {
            Key k = findButton((int) event.getX(), (int) event.getY());
            if (k != null) {
                k.state = Key.DEFAULT;
                animation.add(k, false);
                pressed = null;
                announceKey(k);
            }
            postInvalidate();
        }

        return super.onTouchEvent(event);
    }


    private void announceKey(Key k) {
        if (keyEventListener != null) {
            switch (k.code) {
                case CODE_CANCEL:
                    keyEventListener.onCancel();
                    break;
                case CODE_CORRECT:
                    keyEventListener.onCorrect();
                    break;
                default:
                    keyEventListener.onKey(k.code, k.content);
                    break;
            }
        }
    }

    private Key findButton(int x, int y) {
        for (int i = 0; i < keys.length; i++) {
            Key key = keys[i];
            if (key.r.contains(x, y))
                return key;
        }
        return null;
    }

    private void resolvePress(int x, int y) {
        for (Key key : keys) {
            if (key.r.contains(x, y)) {
                key.state = Key.PRESSED;

                if (pressed != key) {
                    animation.add(key, true);
                }
                pressed = key;
            } else {
                key.state = Key.DEFAULT;
            }
        }

    }

    private List<Key> stopedAnimatios = new ArrayList<>();


    private class PressAnimation extends HashMap<Key, AnimationFactor> implements Runnable {

        public boolean isRunning = false;

        public void add(Key key, boolean grow) {
            if (!containsKey(key))
                put(key, new AnimationFactor(grow));
            if (!isRunning) {
                isRunning = true;
                post(this);
            }

        }

        @Override
        public void run() {
            isRunning = updateValues();
            postInvalidate();
            if (isRunning)
                post(this);
        }

        public boolean updateValues() {
            stopedAnimatios.clear(); // maybe in the end?
            boolean updatable = false;
            boolean current;
            for (Map.Entry<Key, AnimationFactor> entry : entrySet()) {
                if (entry.getKey().state == Key.PRESSED) {
                    current = entry.getValue().up();
                } else {
                    current = entry.getValue().down();
                }
                if (!current)
                    stopedAnimatios.add(entry.getKey());

                updatable |= current;
            }
            for (Key k : stopedAnimatios) {
                remove(k);
            }
            return updatable;
        }

    }


    private static class AnimationFactor {

        long finishIn;
        boolean isGrow;
        float value;

        private AnimationFactor(boolean isGrow) {
            this.isGrow = isGrow;
            value = isGrow ? 0f : 1f;
            update(System.currentTimeMillis());
        }

        boolean up() {
            long current = System.currentTimeMillis();
            if (!isGrow) {
                isGrow = true;
                update(current);
            }

            long delta = finishIn - current;
            if (delta < 0) {
                value = 1f;
                return false;
            }
            value = 1f - ((float) delta) / DEFAULT_ANIMATION_TIME;
            return true;
        }

        boolean down() {
            long current = System.currentTimeMillis();
            if (isGrow) {
                isGrow = false;
                update(current);
            }
            isGrow = false;
            long delta = finishIn - current;
            if (delta < 0) {
                value = 1f;
                return false;
            }
            value = ((float) delta) / DEFAULT_ANIMATION_TIME;
            return true;
        }

        void update(long current) {
            finishIn = current;
            finishIn += (long) ((isGrow ?
                    ((1f - value) / 1f) : (value - 0f) / 1f) * DEFAULT_ANIMATION_TIME);
        }

    }


    public interface KeyEventListener {
        void onCancel();

        void onCorrect();

        void onKey(int code, Character c);
    }


}