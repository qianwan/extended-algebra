package com.yahoo.networkmimo;

import no.uib.cipr.matrix.DenseMatrix;

public interface MIMOChannel {
    DenseMatrix generateMIMOChannel(Entity e);
}
