package com.yahoo.networkmimo;

public class BaseStation extends Entity {
    public BaseStation(double x, double y, int numAntennas) {
        super(x, y, Entity.Type.BS, numAntennas);
    }

}
