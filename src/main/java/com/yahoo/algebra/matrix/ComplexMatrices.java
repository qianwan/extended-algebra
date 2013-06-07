package com.yahoo.algebra.matrix;

public final class ComplexMatrices {
    /**
     * Returns the number of non-zero entries in the given vector
     */
    public static int cardinality(ComplexVector x) {
        int nz = 0;
        for (ComplexVectorEntry e : x)
            if (e.get()[0] != 0 && e.get()[1] != 0)
                nz++;
        return nz;
    }
}
