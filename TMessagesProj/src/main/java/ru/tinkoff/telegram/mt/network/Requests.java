package ru.tinkoff.telegram.mt.network;

/**
 * @author a.shishkin1
 */


public class Requests {
// <GENERATED CODE


    public static final int SIGN_UP = 900;
    public static final int SESSION = 901;
    public static final int MOBILE_SAVE_PIN = 902;
    public static final int MOBILE_AUTH = 903;
    public static final int GET_CONFIRMATION_CODE = 904;
    public static final int TRANSFER_ANY_CARD_TO_ANY_POINTER = 905;
    public static final int TRANSFER_ANY_CARD_TO_ANY_CARD = 906;
    public static final int ATTACH_CARD = 907;
    public static final int DETACH_CARD = 908;
    public static final int ACCOUNTS_FLAT = 909;
    public static final int SET_LINKED_CARD_PRIMARY = 910;
    public static final int PAYMENT_COMMISSION = 911;
    public static final int PAYMENT_RECEIPT = 912;
    public static final int NOW = 913;
    public static final int PING = 914;
    public static final int CONFIRM = 915;
    public static final int CONFIG = 916;
    public static final int RESET_WALLET = 917;
    public static final int RESEND_CODE = 918;

    public static class REQUEST_SIGN_UP {

        public static Network.RequestBuilder prepare(String phone) {
            Network.RequestBuilder result = new Network.RequestBuilder(getAction(), getEndPoint());
            result.addParameter("phone", phone);
            return result;
        }

        private static String getEndPoint() {
            return "sign_up";
        }

        private static int getAction() {
            return SIGN_UP;
        }

    }

    public static class REQUEST_SESSION {

        public static Network.RequestBuilder prepare() {
            Network.RequestBuilder result = new Network.RequestBuilder(getAction(), getEndPoint());
            return result;
        }

        private static String getEndPoint() {
            return "session";
        }

        private static int getAction() {
            return SESSION;
        }

    }

    public static class REQUEST_MOBILE_SAVE_PIN {

        public static Network.RequestBuilder prepare(String pinHash, String currentPinHash) {
            Network.RequestBuilder result = new Network.RequestBuilder(getAction(), getEndPoint());
            result.addParameter("pinHash", pinHash);
            result.addParameter("currentPinHash", currentPinHash);
            return result;
        }

        private static String getEndPoint() {
            return "mobile_save_pin";
        }

        private static int getAction() {
            return MOBILE_SAVE_PIN;
        }

    }

    public static class REQUEST_MOBILE_AUTH {

        public static Network.RequestBuilder prepare(String pinHash, String oldSessionId) {
            Network.RequestBuilder result = new Network.RequestBuilder(getAction(), getEndPoint());
            result.addParameter("pinHash", pinHash);
            result.addParameter("oldSessionId", oldSessionId);
            return result;
        }

        private static String getEndPoint() {
            return "mobile_auth";
        }

        private static int getAction() {
            return MOBILE_AUTH;
        }

    }

    public static class REQUEST_GET_CONFIRMATION_CODE {

        public static Network.RequestBuilder prepare(String user, String initialOperationTicket, String user_session_id) {
            Network.RequestBuilder result = new Network.RequestBuilder(getAction(), getEndPoint());
            result.addParameter("user", user);
            result.addParameter("initialOperationTicket", initialOperationTicket);
            result.addParameter("user_session_id", user_session_id);
            return result;
        }

        private static String getEndPoint() {
            return "get_confirmation_code";
        }

        private static int getAction() {
            return GET_CONFIRMATION_CODE;
        }

    }

    public static class REQUEST_TRANSFER_ANY_CARD_TO_ANY_POINTER {

        public static Network.RequestBuilder prepare(String srcAccountId, String srcNetworkId, String srcName, String dstAccountId, String dstNetworkId, String dstName, String moneyAmount, String currency, String message, String image, String invoice, String ttl, String cardId, String cardNumber, String expiryDate, String securityCode) {
            Network.RequestBuilder result = new Network.RequestBuilder(getAction(), getEndPoint());
            result.addParameter("srcAccountId", srcAccountId);
            result.addParameter("srcNetworkId", srcNetworkId);
            result.addParameter("srcName", srcName);
            result.addParameter("dstAccountId", dstAccountId);
            result.addParameter("dstNetworkId", dstNetworkId);
            result.addParameter("dstName", dstName);
            result.addParameter("moneyAmount", moneyAmount);
            result.addParameter("currency", currency);
            result.addParameter("message", message);
            result.addParameter("image", image);
            result.addParameter("invoice", invoice);
            result.addParameter("ttl", ttl);
            result.addParameter("cardId", cardId);
            result.addParameter("cardNumber", cardNumber);
            result.addParameter("expiryDate", expiryDate);
            result.addParameter("securityCode", securityCode);
            return result;
        }

        private static String getEndPoint() {
            return "transfer_any_card_to_any_pointer";
        }

        private static int getAction() {
            return TRANSFER_ANY_CARD_TO_ANY_POINTER;
        }

    }

    public static class REQUEST_TRANSFER_ANY_CARD_TO_ANY_CARD {

        public static Network.RequestBuilder prepare(String cardNumber, String expiryDate, String securityCode, String cardId, String toCardNumber, String toCardId, String moneyAmount, String currency, String name, String attachCard, String cardName, String screenSize, String timezone) {
            Network.RequestBuilder result = new Network.RequestBuilder(getAction(), getEndPoint());
            result.addParameter("cardNumber", cardNumber);
            result.addParameter("expiryDate", expiryDate);
            result.addParameter("securityCode", securityCode);
            result.addParameter("cardId", cardId);
            result.addParameter("toCardNumber", toCardNumber);
            result.addParameter("toCardId", toCardId);
            result.addParameter("moneyAmount", moneyAmount);
            result.addParameter("currency", currency);
            result.addParameter("name", name);
            result.addParameter("attachCard", attachCard);
            result.addParameter("cardName", cardName);
            result.addParameter("screenSize", screenSize);
            result.addParameter("timezone", timezone);
            return result;
        }

        private static String getEndPoint() {
            return "transfer_any_card_to_any_card";
        }

        private static int getAction() {
            return TRANSFER_ANY_CARD_TO_ANY_CARD;
        }

    }

    public static class REQUEST_ATTACH_CARD {

        public static Network.RequestBuilder prepare(String cardNumber, String expiryDate, String securityCode) {
            Network.RequestBuilder result = new Network.RequestBuilder(getAction(), getEndPoint());
            result.addParameter("cardNumber", cardNumber);
            result.addParameter("expiryDate", expiryDate);
            result.addParameter("securityCode", securityCode);
            return result;
        }

        private static String getEndPoint() {
            return "attach_card";
        }

        private static int getAction() {
            return ATTACH_CARD;
        }

    }

    public static class REQUEST_DETACH_CARD {

        public static Network.RequestBuilder prepare(String cardId) {
            Network.RequestBuilder result = new Network.RequestBuilder(getAction(), getEndPoint());
            result.addParameter("cardId", cardId);
            return result;
        }

        private static String getEndPoint() {
            return "detach_card";
        }

        private static int getAction() {
            return DETACH_CARD;
        }

    }

    public static class REQUEST_ACCOUNTS_FLAT {

        public static Network.RequestBuilder prepare() {
            Network.RequestBuilder result = new Network.RequestBuilder(getAction(), getEndPoint());
            return result;
        }

        private static String getEndPoint() {
            return "accounts_flat";
        }

        private static int getAction() {
            return ACCOUNTS_FLAT;
        }

    }

    public static class REQUEST_SET_LINKED_CARD_PRIMARY {

        public static Network.RequestBuilder prepare(String cardId) {
            Network.RequestBuilder result = new Network.RequestBuilder(getAction(), getEndPoint());
            result.addParameter("cardId", cardId);
            return result;
        }

        private static String getEndPoint() {
            return "set_linked_card_primary";
        }

        private static int getAction() {
            return SET_LINKED_CARD_PRIMARY;
        }

    }

    public static class REQUEST_PAYMENT_COMMISSION {

        public static Network.RequestBuilder prepare(String cardId, String cardNumber, String paymentType, String provider, String currency, String moneyAmount, String fielddstPointerType, String fielddstPointer, String fieldtoCardNumber) {
            Network.RequestBuilder result = new Network.RequestBuilder(getAction(), getEndPoint());
            result.addParameter("cardId", cardId);
            result.addParameter("cardNumber", cardNumber);
            result.addParameter("paymentType", paymentType);
            result.addParameter("provider", provider);
            result.addParameter("currency", currency);
            result.addParameter("moneyAmount", moneyAmount);
            result.addParameter("fielddstPointerType", fielddstPointerType);
            result.addParameter("fielddstPointer", fielddstPointer);
            result.addParameter("fieldtoCardNumber", fieldtoCardNumber);
            return result;
        }

        private static String getEndPoint() {
            return "payment_commission";
        }

        private static int getAction() {
            return PAYMENT_COMMISSION;
        }

    }

    public static class REQUEST_PAYMENT_RECEIPT {

        public static Network.RequestBuilder prepare() {
            Network.RequestBuilder result = new Network.RequestBuilder(getAction(), getEndPoint());
            return result;
        }

        private static String getEndPoint() {
            return "payment_receipt";
        }

        private static int getAction() {
            return PAYMENT_RECEIPT;
        }

    }

    public static class REQUEST_NOW {

        public static Network.RequestBuilder prepare() {
            Network.RequestBuilder result = new Network.RequestBuilder(getAction(), getEndPoint());
            return result;
        }

        private static String getEndPoint() {
            return "now";
        }

        private static int getAction() {
            return NOW;
        }

    }

    public static class REQUEST_PING {

        public static Network.RequestBuilder prepare() {
            Network.RequestBuilder result = new Network.RequestBuilder(getAction(), getEndPoint());
            return result;
        }

        private static String getEndPoint() {
            return "ping";
        }

        private static int getAction() {
            return PING;
        }

    }

    public static class REQUEST_CONFIRM {

        public static Network.RequestBuilder prepare(String secretValue, String confirmationType, String initialOperationTicket, String initialOperation) {
            Network.RequestBuilder result = new Network.RequestBuilder(getAction(), getEndPoint());
            result.addParameter("secretValue", secretValue);
            result.addParameter("confirmationType", confirmationType);
            result.addParameter("initialOperationTicket", initialOperationTicket);
            result.addParameter("initialOperation", initialOperation);
            return result;
        }

        private static String getEndPoint() {
            return "confirm";
        }

        private static int getAction() {
            return CONFIRM;
        }

    }

    public static class REQUEST_CONFIG {

        public static Network.RequestBuilder prepare() {
            Network.RequestBuilder result = new Network.RequestBuilder(getAction(), getEndPoint());
            return result;
        }

        private static String getEndPoint() {
            return "config";
        }

        private static int getAction() {
            return CONFIG;
        }

    }

    public static class REQUEST_RESET_WALLET {

        public static Network.RequestBuilder prepare() {
            Network.RequestBuilder result = new Network.RequestBuilder(getAction(), getEndPoint());
            return result;
        }

        private static String getEndPoint() {
            return "reset_wallet";
        }

        private static int getAction() {
            return RESET_WALLET;
        }

    }

    public static class REQUEST_RESEND_CODE {

        public static Network.RequestBuilder prepare(String initialOperationTicket, String confirmationType, String initialOperation) {
            Network.RequestBuilder result = new Network.RequestBuilder(getAction(), getEndPoint());
            result.addParameter("initialOperationTicket", initialOperationTicket);
            result.addParameter("confirmationType", confirmationType);
            result.addParameter("initialOperation", initialOperation);
            return result;
        }

        private static String getEndPoint() {
            return "resendCode";
        }

        private static int getAction() {
            return RESEND_CODE;
        }

    }



// GENERATED CODE/>




}
