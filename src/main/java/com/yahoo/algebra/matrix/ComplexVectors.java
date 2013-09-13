package com.yahoo.algebra.matrix;

import org.uncommons.maths.random.GaussianGenerator;
import org.uncommons.maths.random.MersenneTwisterRNG;


public class ComplexVectors {
    private static final GaussianGenerator rng;

    static {
        // byte[] seed = new byte[] { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h',
        // 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'};
        rng = new GaussianGenerator(0, 1, new MersenneTwisterRNG());
    }

    public static double getPower(ComplexVector A) {
        double p = 0.0;
        for (ComplexVectorEntry e : A) {
            p += Complexes.abs2(e.get());
        }
        return p;
    }

    public static ComplexVector setPower(ComplexVector A, double power) {
        double currentPower = getPower(A);
        A.scale(new double[] { Math.sqrt(power/currentPower), 0.0 });
        return A;
    }

    public static ComplexVector random(ComplexVector A) {
        for (int i = 0; i < A.size(); i++) {
            A.set(i, new double[]{rng.nextValue(), rng.nextValue()});
        }
        return A;
    }
}
