package com.yahoo.algebra.matrix;

/** Transpose enumeration */
enum Transpose {
    /** Do not transpose */
    NoTranspose,

    /** Transpose */
    Transpose;

    /**
     * @return the netlib character version of this designation, for use with F2J.
     */
    public String netlib() {
        if (this == NoTranspose)
            return "N";
        return "T";
    }

}