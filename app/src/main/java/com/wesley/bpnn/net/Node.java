package com.wesley.bpnn.net;

import android.service.autofill.IFillCallback;

import com.wesley.bpnn.Utils;

import java.util.HashMap;
import java.util.Map;

public abstract class Node {
    private int id;
    protected boolean isExtra = false;

    protected double input;
    protected double output;           //实际输出
    protected double delta;

    private Map<Node,Double> childs = new HashMap<>();

    private Map<Node,Double> parents = new HashMap<>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getInput() {
        return input;
    }

    public void setInput(double input) {
        this.input = input;
    }

    public double getOutput() {
        return output;
    }

    public void setOutput(double output) {
        this.output = output;
    }

    public double getDelta() {
        return delta;
    }

    public boolean isExtra() {
        return isExtra;
    }

    public Map<Node, Double> getChilds() {
        return childs;
    }

    public void addChildNode(Node node,double weight) {
        childs.put(node,weight);
        node.addParentNode(this,weight);
    }

    public void addChildNodeOnly(Node node,double weight) {
        childs.put(node,weight);
        node.addParentNode(this,weight);
    }


    public Double getWeight(Node node){
        if (childs.containsKey(node)){
            return childs.get(node);
        }else if (parents.containsKey(node)){
            return parents.get(node);
        }else {
            return null;
        }
    }

    public Map<Node, Double> getParents() {
        return parents;
    }

    public void addParentNode(Node node,double weight) {
        parents.put(node,weight);
    }

    public double calcOutput(){
        for (Node parent :parents.keySet()){
            Double weight = getWeight(parent);
            if (weight == null){
                throw new NullPointerException("获取权值失败！");
            }
            output += parent.getOutput() * weight.doubleValue();
        }
        output = Utils.sigMoid(output);
        return output;
    }

    public abstract void calcDelta();

    public void updateWeight(){
        for (Node node : getParents().keySet()){
            double weight = node.getWeight(this);
            weight += getDelta() * node.getOutput() * BPNN.studySpeed;
            node.addChildNodeOnly(this,weight);
        }
    }

}
