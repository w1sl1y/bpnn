package com.wesley.bpnn.net;

public class InputNode extends Node {

    public InputNode() {
    }

    public InputNode(double input) {
        setInput(input);
        isExtra = true;
    }

    public boolean isExtra() {
        return isExtra;
    }

    @Override
    public double getOutput() {
        return getInput();
    }

    @Override
    public void calcDelta() {

    }

}
