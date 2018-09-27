package com.wesley.bpnn;

import java.util.Random;

public class Utils {

    static double[] weights = {2,1,-2,3,0,-1,3,-2,-1};
    static  int index = 0;
    /**
     * @return in【-1，1】
     */
    public static double getRandomDouble() {
        double ran = Math.random();
        return ran * 2 - 1;
//        return 1;
//        return weights[index++];
    }

    public static double sigMoid(double value) {
        double ey = Math.pow(Math.E, -value);
        double result = 1 / (1 + ey);
        return result;
    }

}
