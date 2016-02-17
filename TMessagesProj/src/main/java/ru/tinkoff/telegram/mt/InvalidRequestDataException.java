package ru.tinkoff.telegram.mt;

/**
 * @author Mikhail Artemyev
 */
public class InvalidRequestDataException extends RuntimeException{

    public InvalidRequestDataException(String detailMessage) {
        super(detailMessage);
    }

    public InvalidRequestDataException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public InvalidRequestDataException(Throwable throwable) {
        super(throwable);
    }
}
