package com.wesley.bpnn.net;

public class HiddenNode extends Node  {

    public HiddenNode() {
    }

    public HiddenNode(boolean isExtra) {
        this.isExtra = isExtra;
    }



    @Override
    public double getOutput() {
        if (!isExtra){
            return super.getOutput();
        }else {
            return 1;
        }
    }

    @Override
    public void calcDelta() {
        double childDeltaSum = 0;
        for (Node node : getChilds().keySet()){
            childDeltaSum += node.getDelta()*node.getWeight(this);
        }
        delta = output*(1 - output) * childDeltaSum;
    }
}
