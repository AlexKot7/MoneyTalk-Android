package ru.tinkoff.telegram.mt.network.params;

/**
 * @author a.shishkin1
 */


public class CommissionParams {


    private static final String PROVIDER_P2P_C2C = "p2p-c2c";
    private static final String PROVIDER_C2C_ANYTOANY = "c2c-anytoany";


    public static class SRC {
        public final String cardId;
        public final String cardNumber;

        private SRC(String cardId, String cardNumber) {
            this.cardId = cardId;
            this.cardNumber = cardNumber;
        }

        public static SRC forLinkedCard(String cardId) {
            return new SRC(cardId, null);
        }

        public static SRC forNotLinkedCard(String cardNumber) {
            return new SRC(null, cardNumber);
        }
    }

    public static class DEST {

        public final String provider;
        public final String fieldtoCardNumber;
        public final String fielddstPointerType;
        public final String fielddstPointer;


        private DEST(String provider, String fieldtoCardNumber, String fielddstPointerType, String fielddstPointer) {
            this.provider = provider;
            this.fieldtoCardNumber = fieldtoCardNumber;
            this.fielddstPointerType = fielddstPointerType;
            this.fielddstPointer = fielddstPointer;
        }

        public static DEST forCard(String cardNumber) {
            return new DEST(PROVIDER_C2C_ANYTOANY, cardNumber, null, null);
        }

        public static DEST forPhone(String phone) {
            return new DEST(PROVIDER_P2P_C2C, null, "mobile", phone);
        }
    }
}
