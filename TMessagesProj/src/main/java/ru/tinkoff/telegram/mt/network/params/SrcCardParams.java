package ru.tinkoff.telegram.mt.network.params;

import ru.tinkoff.telegram.mt.entities.Card;

/**
 * @author a.shishkin1
 */


public class SrcCardParams  {

    public final String cardId;
    public final String cardNumber;
    public final String expiryDate;
    public final String securityCode;



    public SrcCardParams(Card card) {
        this.cardId = card.getId();
        this.cardNumber = null;
        this.expiryDate = null;
        this.securityCode = null;

    }

    public SrcCardParams(String cardNumber, String expiryDate, String securityCode) {
        this.cardId = null;
        this.cardNumber = cardNumber;
        this.expiryDate = expiryDate;
        this.securityCode = securityCode;
    }
}
