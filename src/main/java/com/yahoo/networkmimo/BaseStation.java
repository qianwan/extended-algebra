package com.yahoo.networkmimo;

public class BaseStation extends Entity {

    BaseStation() {
        super();
        this.setType(Entity.Type.BS);
    }

    BaseStation(double x, double y) {
        super(x, y);
        this.setType(Entity.Type.BS);
    }

    BaseStation(double x, double y, int numAntennas) {
        super(x, y, Entity.Type.BS, numAntennas);
    }

}
