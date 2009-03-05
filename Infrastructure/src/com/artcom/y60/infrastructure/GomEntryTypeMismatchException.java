package com.artcom.y60.infrastructure;

/**
 * Represents the circumstance that a GOM node has been accessed as an attribute
 * or vice versa.
 * 
 * @author arne
 */
public class GomEntryTypeMismatchException extends RuntimeException {

    public GomEntryTypeMismatchException() {
        super();
        // TODO Auto-generated constructor stub
    }

    public GomEntryTypeMismatchException(String detailMessage,
            Throwable throwable) {
        super(detailMessage, throwable);
        // TODO Auto-generated constructor stub
    }

    public GomEntryTypeMismatchException(String detailMessage) {
        super(detailMessage);
        // TODO Auto-generated constructor stub
    }

    public GomEntryTypeMismatchException(Throwable throwable) {
        super(throwable);
        // TODO Auto-generated constructor stub
    }
}
