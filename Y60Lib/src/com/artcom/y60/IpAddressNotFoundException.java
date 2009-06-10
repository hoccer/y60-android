package com.artcom.y60;

public class IpAddressNotFoundException extends Exception {

    public IpAddressNotFoundException(String pMessage) {
        super(pMessage);
    }

    public IpAddressNotFoundException() {
        super();
        // TODO Auto-generated constructor stub
    }

    public IpAddressNotFoundException(String pDetailMessage, Throwable pThrowable) {
        super(pDetailMessage, pThrowable);
        // TODO Auto-generated constructor stub
    }

    public IpAddressNotFoundException(Throwable pThrowable) {
        super(pThrowable);
        // TODO Auto-generated constructor stub
    }

    private static final long serialVersionUID = 1L;

}