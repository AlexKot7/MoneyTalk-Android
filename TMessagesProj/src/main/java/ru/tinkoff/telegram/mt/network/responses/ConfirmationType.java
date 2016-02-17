package ru.tinkoff.telegram.mt.network.responses;

import java.util.HashMap;
import java.util.Map;

/**
 * @author a.shishkin1
 */


public enum ConfirmationType {

    SMS("SMS"),
    SMSBYID("SMSBYID"),
    SMSBYREGISTRATIONID("SMSBYREGISTRATIONID"),
    THREE_DS("3DSecure"),
    LOOP("LOOP");


    private String name;

    ConfirmationType(String name) {
        this.name = name;
    }

    public static final Map<String, ConfirmationType> TYPE_BY_NAME = new HashMap<>();

    static {
        for(ConfirmationType t : values()) {
            TYPE_BY_NAME.put(t.name, t);
        }
    }

    public String getName() {
        return name;
    }


}
