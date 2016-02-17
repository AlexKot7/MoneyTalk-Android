package ru.tinkoff.telegram.mt.utils;

import android.util.Patterns;

import java.util.regex.Pattern;

/**
 * @author Mikhail Artemyev
 */
public class PhoneNumberValidator {

    // +79120000000
    private static final int FULL_NUMBER_LENGTH = 12;

    // country code and first char of operator code
    private static final String RUSSIAN_PHONE_HEAD = "79";

    public static boolean validatePhoneNumber(final String phoneNumber){
        if(phoneNumber == null || phoneNumber.length() == 0){
            return false;
        }

        final boolean formatValid = Patterns.PHONE.matcher(phoneNumber).matches();
        if(!formatValid){
            return false;
        }

        return phoneNumber.startsWith(RUSSIAN_PHONE_HEAD, 1);
    }

}
