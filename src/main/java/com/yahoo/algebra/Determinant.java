package com.yahoo.algebra;

import no.uib.cipr.matrix.DenseLU;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.UpperTriangDenseMatrix;

public final class Determinant {
	private Determinant() {}
	
	public static double det(Matrix A) {
		DenseLU denseLU = DenseLU.factorize(A);
		UpperTriangDenseMatrix U = denseLU.getU();
		double ret = 1.0;
		for (int i=0; i<U.numRows(); i++) {
			ret *= U.get(i, i);
		}
		int []p = denseLU.getPivots();
		for (int i=0; i<p.length; i++) {
			if ((i+1)!=p[i]) ret *= -1.0;
		}
		return ret;
	}
}