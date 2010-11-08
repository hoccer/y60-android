package com.artcom.y60;

import java.io.Serializable;

public class MiddlewareException extends Exception implements Serializable{

	private static final long serialVersionUID = 3475321083263218301L;

	public MiddlewareException() {
        super();
        // TODO Auto-generated constructor stub
    }

    public MiddlewareException(String pDetailMessage, Throwable pThrowable) {
        super(pDetailMessage, pThrowable);
        // TODO Auto-generated constructor stub
    }

    public MiddlewareException(String pDetailMessage) {
        super(pDetailMessage);
        // TODO Auto-generated constructor stub
    }

    public MiddlewareException(Throwable pThrowable) {
        super(pThrowable);
        // TODO Auto-generated constructor stub
    }

}
