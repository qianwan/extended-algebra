package com.yahoo.networkmimo;

import com.yahoo.algebra.matrix.DenseComplexMatrix;

public interface MIMOChannel {
    DenseComplexMatrix generateMIMOChannel(Entity e);
}
