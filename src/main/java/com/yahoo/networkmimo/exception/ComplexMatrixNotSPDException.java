package com.yahoo.networkmimo.exception;

public class ComplexMatrixNotSPDException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = -872248529868915036L;

    public ComplexMatrixNotSPDException() {
        super();
    }

    public ComplexMatrixNotSPDException(String msg) {
        super(msg);
    }
}
