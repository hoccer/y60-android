package com.artcom.y60;

/**
 * To be used for wrapping different kinds of exceptions that can occur when reading data,
 * such as FileNotFoundException, SAXException, SAXParseException, ...
 * 
 * @author arne
 *
 */
public class ReadingDataFailedException extends RuntimeException {

	private static final long serialVersionUID = -1187283029918266338L;

	public ReadingDataFailedException() {
        super();
        // TODO Auto-generated constructor stub
    }

    public ReadingDataFailedException(String pDetailMessage, Throwable pThrowable) {
        super(pDetailMessage, pThrowable);
        // TODO Auto-generated constructor stub
    }

    public ReadingDataFailedException(String pDetailMessage) {
        super(pDetailMessage);
        // TODO Auto-generated constructor stub
    }

    public ReadingDataFailedException(Throwable pThrowable) {
        super(pThrowable);
        // TODO Auto-generated constructor stub
    }
}
