package com.yahoo.networkmimo;

import com.yahoo.algebra.matrix.ComplexMatrix;

public interface MIMOChannel {
    ComplexMatrix genenerateMIMOChannel(Entity e);
}
