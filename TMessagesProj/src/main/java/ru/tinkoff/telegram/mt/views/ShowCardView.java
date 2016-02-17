package ru.tinkoff.telegram.mt.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import ru.tinkoff.telegram.mt.utils.CardLogoCache;

/**
 * @author a.shishkin1
 */


public class ShowCardView extends LinearLayout {

    private ImageView ivSystemLogo;
    private TextView tvCardNumber;

    public ShowCardView(Context context) {
        super(context);
        init();
    }

    public ShowCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ShowCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    protected void init() {
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        Context context = getContext();
        tvCardNumber = new TextView(context);
        ivSystemLogo = new ImageView(context);
        addView(ivSystemLogo);
        addView(tvCardNumber);
        ((LayoutParams)tvCardNumber.getLayoutParams()).weight = 1;
    }

    public void show(String cardName, String cardValue) {
        tvCardNumber.setText(cardName);
        ivSystemLogo.setImageBitmap(CardLogoCache.getLogoByNumber(getContext(), cardValue));
    }



}
