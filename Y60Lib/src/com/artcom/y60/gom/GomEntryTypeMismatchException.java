package com.artcom.y60.gom;

/**
 * Represents the circumstance that a GOM node has been accessed as an attribute
 * or vice versa.
 * 
 * @author arne
 */
public class GomEntryTypeMismatchException extends GomException {

	private static final long serialVersionUID = 937733259134130588L;

	public GomEntryTypeMismatchException() {
        super();
        // TODO Auto-generated constructor stub
    }

    public GomEntryTypeMismatchException(String detailMessage, Throwable throwable) {
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
