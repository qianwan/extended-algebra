package com.yahoo.algebra.matrix;

import java.io.Serializable;

public interface ComplexVector extends Iterable<ComplexVectorEntry>, Serializable{

    /**
     * Size of the vector
     */
    int size();

    /**
     * <code>x(index) = value</code>
     */
    void set(int index, double value[]);

    /**
     * <code>x(index) += value</code>
     */
    void add(int index, double value[]);

    /**
     * Returns <code>x(index)</code>
     */
    double[] get(int index);

    /**
     * Creates a deep copy of the vector
     */
    ComplexVector copy();

    /**
     * Zeros all the entries in the vector, while preserving any underlying
     * structure
     */
    ComplexVector zero();

    /**
     * <code>x=alpha*x</code>
     * 
     * @return x
     */
    ComplexVector scale(double alpha[]);

    /**
     * <code>x=y</code>
     * 
     * @return x
     */
    ComplexVector set(ComplexVector y);

    /**
     * <code>x=alpha*y</code>
     * 
     * @return x
     */
    ComplexVector set(double alpha[], ComplexVector y);

    /**
     * <code>x = y + x</code>
     * 
     * @return x
     */
    ComplexVector add(ComplexVector y);

    /**
     * <code>x = alpha*y + x</code>
     * 
     * @return x
     */
    ComplexVector add(double alpha[], ComplexVector y);

    /**
     * <code>x<sup>T</sup>*y</code>
     */
    double[] dot(ComplexVector y);

    /**
     * Computes the given norm of the vector
     * 
     * @param type
     *            The type of norm to compute
     */
    double norm(Norm type);

    /**
     * Supported vector-norms. The difference between the two 2-norms is that
     * one is fast, but can overflow, while the robust version is overflow
     * resistant, but slower.
     */
    enum Norm {

        /**
         * Sum of the absolute values of the entries
         */
        One,

        /**
         * The root of sum of squares
         */
        Two,

        /**
         * As the 2 norm may overflow, an overflow resistant version is also
         * available. Note that it may be slower.
         */
        TwoRobust,

        /**
         * Largest entry in absolute value
         */
        Infinity

    }
}
