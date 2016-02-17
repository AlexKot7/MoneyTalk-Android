package ru.tinkoff.telegram.mt.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.SparseArray;

import ru.tinkoff.telegram.mt.R;

import java.lang.ref.WeakReference;

/**
 * @author a.shishkin1
 */


public class CardLogoCache {

    private static SparseArray<WeakReference<Bitmap>> sCache = new SparseArray<>();

    public static Bitmap getLogoByNumber(Context context, String cardNumber) {
        if(TextUtils.isEmpty(cardNumber)) {
            return null;
        }
        char fc = cardNumber.charAt(0);
        int resId = resIdByChar(fc);
        if(resId == 0) {
            return null;
        }
        Bitmap result = null;
        WeakReference<Bitmap> weakReference = sCache.get(resId);
        if(weakReference != null) {
            result = weakReference.get();
            if(result != null) {
                return result;
            }
        }
        result = BitmapFactory.decodeResource(context.getResources(), resId);
        sCache.put(resId, new WeakReference<>(result));
        return result;
    }

    private static int resIdByChar(char c) {
        switch (c) {
            case '2':
                return R.drawable.master;
            case '4':
                return R.drawable.visalogo;
            case '5':
                return R.drawable.master;
            case '6':
                return R.drawable.maestro;
        }
        return 0;
    }

}
