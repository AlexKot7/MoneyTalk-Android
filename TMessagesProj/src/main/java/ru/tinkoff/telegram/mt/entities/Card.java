package ru.tinkoff.telegram.mt.entities;

import android.text.TextUtils;
import android.util.SparseArray;

import org.json.JSONObject;

import ru.tinkoff.telegram.mt.network.responses.IJsonable;

/**
 * @author a.shishkin1
 */


public class Card implements IJsonable {

    private String id;
    private String value;
    private boolean cvcConfirmRequired;
    private boolean primary;
    private String bankName;

    private transient String cardName;

    public String getId() {
        return id;
    }

    public String getValue() {
        return value;
    }

    public String getCardName() {
        if(cardName == null) {
            int l = value.length();
            cardName = bankName + " *" + value.substring(Math.max(0, l - 4), l);
        }
        return cardName;
    }

    public boolean isCvcConfirmRequired() {
        return cvcConfirmRequired;
    }

    public boolean isPrimary() {
        return primary;
    }

    @Override
    public void fillByJson(JSONObject jobj) {
        id = jobj.optString("id");
        value = jobj.optString("value");
        cvcConfirmRequired = jobj.optBoolean("cvcConfirmRequired");
        primary = jobj.optBoolean("primary");
        JSONObject jBankInfo = jobj.optJSONObject("lcsCardInfo");
        if(jBankInfo != null) {
            bankName = jBankInfo.optString("bankName");
        }
    }

    @Override
    public JSONObject createJson() {
        throw new UnsupportedOperationException();
    }


    private static SparseArray<CardType> cardsFirstDigits = new SparseArray<CardType>() {{
        put('2', CardType.MASTER_CARD);
        put('4', CardType.VISA);
        put('5', CardType.MASTER_CARD);
        put('6', CardType.MAESTRO);
    }};

    public static CardType recognize(String knownDigits) {
        if (TextUtils.isEmpty(knownDigits)) {
            return CardType.UNKNOWN;
        }

        char firstDigit = knownDigits.charAt(0);

        return cardsFirstDigits.get(firstDigit,
                CardType.UNKNOWN);
    }


    public enum CardType {
        VISA,
        MASTER_CARD,
        MAESTRO,
        UNKNOWN
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }
}
