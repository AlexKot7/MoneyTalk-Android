package ru.tinkoff.telegram.mt.entities;

import android.content.Context;

import org.json.JSONObject;

import java.math.BigDecimal;

import ru.tinkoff.telegram.mt.R;
import ru.tinkoff.telegram.mt.network.responses.IJsonable;
import ru.tinkoff.telegram.mt.utils.Utils;

/**
 * @author a.shishkin1
 */


public class Commission implements IJsonable {

    private String description;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private BigDecimal value;


    private transient String valueString;



    @Override
    public void fillByJson(JSONObject jobj) {
        JSONObject jvalue = jobj.optJSONObject("value");
        if(jvalue != null) {
            String valueString = jvalue.optString("value");
            if(valueString != null) {
                value = new BigDecimal(valueString);
            }
        }
        description = jobj.optString("description");
        String minAmountString = jobj.optString("minAmount");
        String maxAmountString = jobj.optString("maxAmount");
        if(minAmountString != null) {
            minAmount = new BigDecimal(minAmountString);
        }
        if(maxAmountString != null) {
            maxAmount = new BigDecimal(maxAmountString);
        }
    }

    @Override
    public JSONObject createJson() {
        throw new UnsupportedOperationException();
    }


    public String createHumanReadableString(Context context) {
        if(valueString == null) {
            if (value.compareTo(BigDecimal.ZERO) > 0) {
                valueString = String.format(context.getString(R.string.mt_commission_value), Utils.MONEY_FORMAT.format(value));
            } else {
                valueString = context.getString(R.string.mt_commission_not_need);
            }
        }
        return valueString;
    }

    public static class HumanReadable {
        public static String createHumanReadableString(Commission commission, Context context) {
            if(commission == null) {
                return context.getString(R.string.mt_commission_fail);
            }
            return commission.createHumanReadableString(context);
        }
    }

}
