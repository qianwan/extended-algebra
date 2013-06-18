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

    private Network network;

    private final List<BaseStation> bss = new ArrayList<BaseStation>();

    private final List<UE> ues = new ArrayList<UE>();

    /**
     * transmit precoding matrix for every UE in this cluster
     */
    private final Map<UE, DenseComplexMatrix> txPreMatrix = Maps.newHashMap();

    private ComplexMatrix jMatrix;

    public ComplexMatrix getJMatrix() {
        return jMatrix;
    }

    public Cluster() {
        super();
        setType(Entity.Type.CLUSTER);
    }

    public Cluster(double x, double y) {
        super(x, y);
        setType(Entity.Type.CLUSTER);
    }

    public Cluster addBaseStation(BaseStation bs) {
        getBSs().add(bs);
        bs.setCluster(this);
        bs.setNetwork(network);
        setNumAntennas(getNumAntennas() + bs.getNumAntennas());
        return this;
    }

    public Cluster addUE(UE ue) {
        getUEs().add(ue);
        ue.setCluster(this);
        ue.setNetwork(network);
        return this;
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
            if (txPreMatrix.get(ue) == null) {
                txPreMatrix.put(ue, new DenseComplexMatrix(getNumAntennas(), ue.getNumStreams()));
            }
        }
    }

    public void assembleTxPreMatrix() {
        isReady();
        for (UE ue : getUEs()) {
            ComplexMatrix vik = txPreMatrix.get(ue);
            int rowOffset = 0;
            for (BaseStation bs : getBSs()) {
                ComplexMatrix vikq = bs.getTxPreMatrix(ue);
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

    public ComplexMatrix getTxPreMatrix(UE ue) {
        return txPreMatrix.get(ue);
    }

    public void isReady() {
        for (UE ue : getUEs()) {
            ComplexMatrix Vik = txPreMatrix.get(ue);
            if (Vik == null) {
                throw new ClusterNotReadyException("tx precoding matrix for ue is null");
            } else {
                if (Vik.numRows() != getNumAntennas() || Vik.numColumns() != ue.getNumStreams()) {
                    throw new ClusterNotReadyException(
                            "cluster configuration is not compatabile with size of tx precoding matrix");
                }
            }
        }
    }

    public ComplexMatrix calcJMatrix() {
        // H^H * u * u^H * H
        jMatrix.zero();
        for (Cluster cluster : network.getClusters()) {
            for (UE ue : cluster.getUEs()) {
                ComplexMatrix Hhu = getMIMOChannel(ue).hermitianTranspose().mult(ue.getRxPreMatrix());
                ComplexMatrix HuuH = Hhu.mult(Hhu.hermitianTranspose());
                // TODO for numStreams == 1
                HuuH.scale(ue.getMMSEWeight().get(0, 0));
                jMatrix.add(HuuH);
            }
        }
        return jMatrix;
    }

    public Network getNetwork() {
        return network;
    }

    public void setNetwork(Network network) {
        this.network = network;
    }

    public void alloc() {
        // self init
        jMatrix = new DenseComplexMatrix(getNumAntennas(), getNumAntennas());
        for (UE ue : ues) {
            txPreMatrix.put(ue, new DenseComplexMatrix(getNumAntennas(), ue.getNumStreams()));
            mimoChannels.put(ue, new DenseComplexMatrix(ue.getNumAntennas(), getNumAntennas()));
        }
        // component init
        for (BaseStation bs : bss) {
            bs.alloc();
        }
        for (UE ue : ues) {
            ue.alloc();
        }
    }

    public void genRandomTxPreMatrix() {
        for (BaseStation bs : bss) {
            bs.genRandomTxPreMatrix();
        }
        assembleTxPreMatrix();
    }

    /**
     * 
     * @param ue UE
     */
    public void genMIMOChannel(UE ue) {
        for (BaseStation bs : bss) {
            bs.genMIMOChannel(ue);
        }
        ComplexMatrix H = mimoChannels.get(ue);
        int columnOffset = 0;
        for (BaseStation bs : bss) {
            ComplexMatrix bsH = bs.getMIMOChannel(ue);
            for (ComplexMatrixEntry entry : bsH) {
                H.set(entry.row(), entry.column() + columnOffset, entry.get());
            }
            columnOffset += bs.getNumAntennas();
        }
    }
}
