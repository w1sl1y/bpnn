package com.wesley.bpnn.net;

public class OutputNode extends Node  {
    private double exp;             //期望输出

    public void setExp(double exp) {
        this.exp = exp;
    }

    public double getExp() {
        return exp;
    }

    @Override
    public void calcDelta() {
        delta = (exp - output)*output*(1 - output);
    }
}
