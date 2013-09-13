package com.yahoo.networkmimo.exception;

public class ComplexVectorNotSPDException extends RuntimeException {
    private static final long serialVersionUID = 6732879671753066246L;

    public ComplexVectorNotSPDException() {
        super();
    }
    public ComplexVectorNotSPDException(String msg) {
        super(msg);
    }
}
