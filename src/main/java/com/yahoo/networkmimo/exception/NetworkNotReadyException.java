package com.yahoo.networkmimo.exception;

public class NetworkNotReadyException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = -6921740740850769412L;

    public NetworkNotReadyException() {
        super();
    }

    public NetworkNotReadyException(String msg) {
        super(msg);
    }
}
