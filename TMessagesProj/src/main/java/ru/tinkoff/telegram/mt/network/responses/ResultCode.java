package ru.tinkoff.telegram.mt.network.responses;

import java.util.HashMap;
import java.util.Map;

/**
 * @author a.shishkin1
 */


public enum ResultCode {

    OK,
    NOT_AUTHENTICATED,
    AUTHENTICATION_FAILED,
    USER_LOCKED,
    OPERATION_REJECTED,
    WAITING_CONFIRMATION,
    ROLE_ESCALATION,
    INSUFFICIENT_PRIVILEGES,
    REQUEST_RATE_LIMIT_EXCEEDED,
    INVALID_REQUEST_DATA,
    INVALID_PASSWORD,
    INTERNAL_ERROR,
    NOT_IDENTIFICATED,
    CONFIRMATION_FAILED,
    BANK_SERVICE_DISABLED,
    CONFIRMATION_EXPIRED,
    ROLE_NOT_GRANTED,
    WRONG_CONFIRMATION_CODE,
    WRONG_OPERATION_TICKET,
    RESEND_FAILED,
    WRONG_PIN_CODE,
    PIN_ATTEMPS_EXCEEDED,
    PIN_IS_NOT_SET,
    DEVICE_ALREADY_LINKED,
    DEVICE_LINK_NEEDED,
    NO_DATA_FOUND,
    TOKEN_EXPIRED;


    public static final Map<String, ResultCode> CODES_BY_NAME = new HashMap<>();

    static {
        for (ResultCode rc : ResultCode.values()) {
            CODES_BY_NAME.put(rc.toString(), rc);
        }
    }


}



