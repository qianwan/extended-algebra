package com.yahoo.networkmimo;

import static java.lang.Math.*;

public final class Utils {
    public static double getEntityDistance(Entity e1, Entity e2) {
        double dist = 0.0;
        double [] pos1 = e1.getXY();
        double x1 = pos1[0];
        double y1 = pos1[1];
        double [] pos2 = e2.getXY();
        double x2 = pos2[0];
        double y2 = pos2[1];
        dist = sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2));
        return dist;
    }

    public static double log2(double a) {
        return Math.log(a) / Math.log(2);
    }
}
