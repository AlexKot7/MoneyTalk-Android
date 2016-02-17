package ru.tinkoff.telegram.mt.utils;

import android.text.TextPaint;
import android.text.style.CharacterStyle;
import android.text.style.UpdateAppearance;

/**
 * @author a.shishkin1
 */


public class MutableColorSpan extends CharacterStyle implements UpdateAppearance {


    private int color;

    public MutableColorSpan(int color) {
        super();
        this.color = color;
    }


    public void setAlpha(int alpha) {
        color = (color & 0x00FFFFFF) | (alpha << 24);
    }


    @Override
    public void updateDrawState(TextPaint tp) {
        tp.setColor(color);
    }
}
