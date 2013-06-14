package com.yahoo.algebra.matrix;

public final class Complexes {
    private Complexes() {
    }

    public static double[] mult(double[] a, double[] b) {
        double re = a[0] * b[0] - a[1] * b[1];
        double im = a[1] * b[0] + a[0] * b[1];
        return new double[] { re, im };
    }

    public static double[] add(double[] a, double[] b) {
        return new double[] { a[0] + b[0], a[1] + b[1] };
    }

    public static double[] conjugate(double[] a) {
        return new double[] { a[0], -a[1] };
    }

    public static double abs(double[] a) {
        return Math.sqrt(a[0] * a[0] + a[1] * a[1]);
    }

    public static double abs2(double[] a) {
        return a[0] * a[0] + a[1] * a[1];
    }
}
