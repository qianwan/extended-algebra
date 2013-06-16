package com.yahoo.networkmimo.exception;

public class NetworkNotReady extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = -6921740740850769412L;

    public NetworkNotReady() {
        super();
    }

    public NetworkNotReady(String msg) {
        super(msg);
    }
}
