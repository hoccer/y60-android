package com.artcom.y60;

/**
 * To be used if processing data from an external data source (such as JSON or XML data)
 * failed because its content wasn't as expected, e.g. an element is missing or an
 * attribute has a wrong type.
 * 
 * Its NOT meant to be used parsing exceptions, e.g. to wrap a JSON exception because the
 * JSON is not properly formatted.
 * 
 * @author arne
 *
 */
public class DefectiveContentException extends Exception {

    public DefectiveContentException() {
        super();
        // TODO Auto-generated constructor stub
    }

    public DefectiveContentException(String pDetailMessage, Throwable pThrowable) {
        super(pDetailMessage, pThrowable);
        // TODO Auto-generated constructor stub
    }

    public DefectiveContentException(String pDetailMessage) {
        super(pDetailMessage);
        // TODO Auto-generated constructor stub
    }

    public DefectiveContentException(Throwable pThrowable) {
        super(pThrowable);
        // TODO Auto-generated constructor stub
    }

}
