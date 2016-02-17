package ru.tinkoff.telegram.mt;

/**
 * @author a.shishkin1
 */


public class NotImplementedYetException extends RuntimeException {





    public NotImplementedYetException() {
        super("NOT IMPLEMENTED YET");
    }

    public NotImplementedYetException(String detailMessage) {
        super(detailMessage);
    }
}
