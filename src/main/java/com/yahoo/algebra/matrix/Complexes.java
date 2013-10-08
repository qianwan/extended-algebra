package com.yahoo.algebra.matrix;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Complexes {
    private static final Pattern doublePattern = Pattern
            .compile("[-+]?(\\d+\\.\\d+|\\d+\\.|\\.\\d+|\\d+)([eE][-+]?\\d+)?");

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

    public static boolean equals(double[] a, double[] b, double delta) {
        double[] c = new double[] { a[0] - b[0], a[1] - b[1] };
        double absc = abs(c);
        return absc < delta;
    }

    public static double[] read(String s) {
        double[] c = new double[2];
        Matcher m = doublePattern.matcher(s);
        m.find();
        c[0] = Double.parseDouble(m.group());
        m.find();
        c[1] = Double.parseDouble(m.group());
        return c;
    }
}
