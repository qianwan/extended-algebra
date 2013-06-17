package com.yahoo.networkmimo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.internal.Maps;
import com.yahoo.algebra.matrix.ComplexMatrix;
import com.yahoo.algebra.matrix.ComplexMatrixEntry;
import com.yahoo.algebra.matrix.DenseComplexMatrix;
import com.yahoo.networkmimo.exception.ClusterNotReadyException;

public class Cluster extends Entity {
    private final static Logger logger = LoggerFactory.getLogger(Cluster.class);

    private final List<BaseStation> bss = new ArrayList<BaseStation>();

    private final List<UE> ues = new ArrayList<UE>();

    private final Map<UE, DenseComplexMatrix> txPrecodingMatrix = Maps.newHashMap();

    public Cluster() {
        super();
        setType(Entity.Type.CLUSTER);
    }

    public Cluster(double x, double y) {
        super(x, y);
        setType(Entity.Type.CLUSTER);
    }

    public void addBaseStation(BaseStation bs) {
        getBSs().add(bs);
        bs.setCluster(this);
        setNumAntennas(getNumAntennas() + bs.getNumAntennas());
    }

    public void addUE(UE ue) {
        getUEs().add(ue);
        ue.setCluster(this);
    }

    @Override
    public ComplexMatrix getMIMOChannel(Entity e) {
        ComplexMatrix H = null;
        if (e instanceof UE) {
            H = new DenseComplexMatrix(e.getNumAntennas(), getNumAntennas());
            int columnOffset = 0;
            for (BaseStation bs : getBSs()) {
                ComplexMatrix bsH = bs.getMIMOChannel(e);
                for (ComplexMatrixEntry entry : bsH) {
                    H.set(entry.row(), entry.column() + columnOffset, entry.get());
                }
                columnOffset += bs.getNumAntennas();
            }
            setMIMOChannel(e, H);
        } else {
            logger.info("only downlink supported");
        }
        return H;
    }

    public void initTxPrecodingMatrix() {
        for (UE ue : getUEs()) {
            if (txPrecodingMatrix.get(ue) == null) {
                txPrecodingMatrix.put(ue,
                        new DenseComplexMatrix(getNumAntennas(), ue.getNumStreams()));
            }
        }
    }

    public void updateTxPrecodingMatrix() throws ClusterNotReadyException {
        isReady();
        for (UE ue : getUEs()) {
            DenseComplexMatrix vik = txPrecodingMatrix.get(ue);
            int rowOffset = 0;
            for (BaseStation bs : getBSs()) {
                ComplexMatrix vikq = bs.getTxPrecodingMatrix(ue);
                for (ComplexMatrixEntry entry : vikq) {
                    vik.set(entry.row() + rowOffset, entry.column(), entry.get());
                }
                rowOffset += bs.getNumAntennas();
            }
        }
    }

    /**
     * @return the bss
     */
    public List<BaseStation> getBSs() {
        return bss;
    }

    /**
     * @return the ues
     */
    public List<UE> getUEs() {
        return ues;
    }

    public ComplexMatrix getTxPrecodingMatrix(UE ue) {
        return txPrecodingMatrix.get(ue);
    }

    public void isReady() throws ClusterNotReadyException {
        for (UE ue : getUEs()) {
            ComplexMatrix Vik = txPrecodingMatrix.get(ue);
            if (Vik == null) {
                throw new ClusterNotReadyException("tx precoding matrix for ue is null");
            } else {
                if (Vik.numRows() != getNumAntennas() || Vik.numColumns() != ue.getNumAntennas()) {
                    throw new ClusterNotReadyException(
                            "cluster configuration is not compatabile with size of tx precoding matrix");
                }
            }
        }
    }
}
