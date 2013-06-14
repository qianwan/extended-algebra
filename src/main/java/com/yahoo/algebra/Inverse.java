package com.yahoo.algebra;

import org.apache.log4j.Logger;
import org.netlib.lapack.LAPACK;
import org.netlib.util.intW;

import no.uib.cipr.matrix.DenseMatrix;

public class Inverse {
    private static Logger logger = Logger.getLogger(Inverse.class);

    private Inverse() {
    }

    public static DenseMatrix inv(DenseMatrix A) {
        if (!A.isSquare()) {
            logger.error("Matrix to be inversed is not square");
            return null;
        }

        intW info = new intW(0);
        DenseMatrix B = A.copy();
        int m = B.numRows();
        int[] piv = new int[B.numRows()];

        LAPACK.getInstance().dgetrf(m, m, B.getData(), m, piv, info);

        if (info.val != 0)
            return null;
        int lwork = m;
        double[] work = new double[m];
        LAPACK.getInstance().dgetri(m, B.getData(), m, piv, work, lwork, info);
        return B;
    }
}
