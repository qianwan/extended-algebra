package com.yahoo.networkmimo.exception;

public class ClusterNotReadyException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = -4246068471468131993L;

    public ClusterNotReadyException() {
        super();
    }

    public ClusterNotReadyException(String msg) {
        super(msg);
    }
}
