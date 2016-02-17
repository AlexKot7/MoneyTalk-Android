package ru.tinkoff.telegram.mt.utils;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.graphics.drawable.shapes.Shape;
import android.util.TypedValue;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;

import ru.tinkoff.telegram.mt.ui.TgR;

/**
 * @author a.shishkin1
 */


public class Utils {

    public static final DecimalFormat MONEY_FORMAT = new DecimalFormat("#,##0.##");

    public static String md5(String input) {
        try {
            // Create MD5 Hash
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte messageDigest[] = digest.digest(input.getBytes());

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String hex = Integer.toHexString(0xFF & aMessageDigest);
                // Append leading '0' for one digit ints
                if (hex.length() == 1) {
                    hexString.append("0").append(hex);
                } else {
                    hexString.append(hex);
                }
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static StateListDrawable createItemsStateListDrawable() {
        StateListDrawable sld = new StateListDrawable();

        sld.addState(new int[] { android.R.attr.state_pressed }, new ColorDrawable(0x33D1D1D1));
        sld.addState(new int[] { android.R.attr.state_enabled }, new ColorDrawable(0x00FFFFFF));

        return sld;
    }


    public static ColorStateList createBlueTextListColor() {
        ColorStateList csl = new ColorStateList(
                new int[][] {
                        new int[]{android.R.attr.state_pressed},
                        new int[]{android.R.attr.state_enabled }
                },
                new int[] {
                        TgR.color.main_theme_color,
                        TgR.color.blue_color
                }
        );

        return csl;
    }



    public static StateListDrawable createBlueButtonDrawable(Context context) {

        float corner = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, context.getResources().getDisplayMetrics());

        float[] corners = new float[] { corner, corner, corner, corner, corner, corner, corner, corner};

        ShapeDrawable shapeDrawable = new ShapeDrawable();
        shapeDrawable.getPaint().setColor(TgR.color.blue_color);
        Shape rrs = new RoundRectShape(corners, null, null);
        shapeDrawable.setShape(rrs);

        ShapeDrawable shapeDrawablePressed = new ShapeDrawable();
//        shapeDrawablePressed.getPaint().setColor((TgR.color.blue_color & 0x00FFFFFF) | (0xCC << 24));
        shapeDrawablePressed.getPaint().setColor(TgR.color.main_theme_color);
        Shape rrsPressed = new RoundRectShape(corners, null, null);
        shapeDrawablePressed.setShape(rrsPressed);

        ShapeDrawable shapeDrawableDisable = new ShapeDrawable();
        shapeDrawableDisable.getPaint().setColor(0xFFD1D1D1);
        Shape rrsDisable = new RoundRectShape(corners, null, null);
        shapeDrawableDisable.setShape(rrsDisable);

        StateListDrawable sld = new StateListDrawable();
        sld.addState(new int[] { -android.R.attr.state_enabled }, shapeDrawableDisable);
        sld.addState(new int[] { android.R.attr.state_pressed }, shapeDrawablePressed);
        sld.addState(new int[] { android.R.attr.state_enabled }, shapeDrawable);

        return sld;
    }






}
