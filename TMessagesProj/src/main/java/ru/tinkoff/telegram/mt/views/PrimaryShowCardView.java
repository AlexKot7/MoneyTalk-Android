package ru.tinkoff.telegram.mt.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

import ru.tinkoff.telegram.mt.R;

/**
 * @author a.shishkin1
 */


public class PrimaryShowCardView extends ShowCardView {

    private TextView tvPrimary;

    public PrimaryShowCardView(Context context) {
        super(context);
    }

    public PrimaryShowCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PrimaryShowCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void init() {
        super.init();
        tvPrimary = new TextView(getContext());
        tvPrimary.setText(R.string.mt_primary);
        tvPrimary.setTextColor(0xffd1d1d1);
        tvPrimary.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        addView(tvPrimary);
        setPrimary(false);
    }

    public void setPrimary(boolean isPrimary) {
        tvPrimary.setVisibility(isPrimary ? VISIBLE : GONE);
    }


    public void show(String cardName, String cardValue, boolean isPrimary) {
        setPrimary(isPrimary);
        super.show(cardName, cardValue);
    }
}
