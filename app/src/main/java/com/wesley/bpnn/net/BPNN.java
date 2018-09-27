package com.wesley.bpnn.net;

import android.util.Log;

import com.wesley.bpnn.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class BPNN {
    public static final String TAG = "BPNN";
    private List<Node> inputs = new ArrayList<>();
    private List<Node> outputs = new ArrayList<>();
    private List<List<Node>> hiddenNodes = new ArrayList<>();

    public static double studySpeed = 1;

    private double lost = 0;

    private int step;                   //当前已经执行了几次学习

    private int threashholdDepth;       //循环次数的阈值

    private double threashholdLost = 0.01;        //误差的阈值

    public double getLost(){
        return lost;
    }

    public int getStep() {
        return step;
    }

    public int getThreashholdDepth() {
        return threashholdDepth;
    }

    public void setThreashholdDepth(int threashholdDepth) {
        this.threashholdDepth = threashholdDepth;
    }

    public double getThreashholdLost() {
        return threashholdLost;
    }

    public void setThreashholdLost(double threashholdLost) {
        this.threashholdLost = threashholdLost;
    }

    public void clear(){
        inputs.clear();
        outputs.clear();
        hiddenNodes.clear();
        step = 0;
    }

    public  double calcLost(){
        lost = 0;
        synchronized (outputs){
            for (Node node : outputs){
                OutputNode outputNode = (OutputNode) node;
                lost += Math.pow(outputNode.getExp() - outputNode.getOutput(),2);
            }
            lost = lost/2;
        }
        String log = String.format("after %d steps,lost fun result = %f",step,lost);
        Log.i(TAG, log);

        return lost;
    }

    /**
     * 一旦执行了该方法，则网络初始化完毕，接下来就是计算的过程了
     */
    public void randomWeights(){
        if (hiddenNodes.size() == 0){
            return;
        }

        for (Node node : inputs){
            for (Node hiddenNode : hiddenNodes.get(0)){
                if (!hiddenNode.isExtra()){
                    node.addChildNode(hiddenNode, Utils.getRandomDouble());
                }
            }
        }


        if (hiddenNodes.size() > 1){
            //不止一层隐藏层
            for (int i = 0; i < hiddenNodes.size(); i++) {
                if (i < hiddenNodes.size() - 1){
                    for (Node node : hiddenNodes.get(i)){
                        for (Node child : hiddenNodes.get(i+ 1)){
                            if (!child.isExtra())
                                node.addChildNode(child, Utils.getRandomDouble());
                        }
                    }
                }else {
                    //最后一层，要链接输出层了
                    for (Node node : hiddenNodes.get(i)){
                        for (Node child : outputs){
                            node.addChildNode(child, Utils.getRandomDouble());
                        }
                    }
                }

            }
        }else {
            //隐藏层直接链接输出层
            for (Node hiddenNode : hiddenNodes.get(0)){
                for (Node child : outputs) {
                    hiddenNode.addChildNode(child, Utils.getRandomDouble());
                }
            }
        }

    }

    public void initInputs(int size){
        for (int i = 0; i < size; i++) {
            Node node = new InputNode();
            inputs.add(node);
        }
        inputs.add(new InputNode(1));
    }

    public void addHiddenLayer(int size){
        List<Node> hiddens = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Node node = new HiddenNode();
            hiddens.add(node);
        }
        hiddens.add(new HiddenNode(true));
        hiddenNodes.add(hiddens);
    }

    public void addOutputs(int size){
        for (int i = 0; i < size; i++) {
            Node node = new OutputNode();
            outputs.add(node);
        }
    }

    public double studyOneStep(double[] feats,double[] exps){
        if (feats.length != inputs.size() - 1){
            String errMsg = String.format("输入特征参数错误，需要特征参数%d个,实际参数%d个",inputs.size() - 1 ,feats.length);
            throw new IllegalArgumentException(errMsg);
        }

        if (exps.length != outputs.size()){
            String errMsg = String.format("期望输出参数错误，需要期望输出%d个,实际参数%d个",outputs.size(),exps.length);
            throw new IllegalArgumentException(errMsg);
        }

        for (int i = 0; i < feats.length; i++){
            Node node = inputs.get(i);
            node.setInput(feats[i]);
        }

        for (List<Node> hiddens : hiddenNodes){
            for (Node hiddenNode : hiddens){
                hiddenNode.calcOutput();
            }
        }

        for (int i = 0; i < exps.length; i++){
            OutputNode node = (OutputNode) outputs.get(i);
            node.setExp(exps[i]);
            node.calcOutput();
        }

        step++;
        return lost;
    }

    public void backPropagation(){
        //从后往前计算delta
        for (Node outNode : outputs){
            outNode.calcDelta();
        }

        for (int i = hiddenNodes.size() - 1; i >= 0; i--){
            for (Node node : hiddenNodes.get(i)){
                node.calcDelta();
            }
        }

        //从前往后更新权值
        for (int i = 0; i < hiddenNodes.size(); i++){
            for (Node node : hiddenNodes.get(i)){
                node.updateWeight();
            }
        }

        for (Node outNode : outputs){
            outNode.updateWeight();
        }

    }

    public double[] predict(double[] feats){
        if (feats.length != inputs.size() - 1){
            String errMsg = String.format("输入特征参数错误，需要特征参数%d个,实际参数%d个",inputs.size() - 1 ,feats.length);
            throw new IllegalArgumentException(errMsg);
        }

        for (int i = 0; i < feats.length; i++){
            Node node = inputs.get(i);
            node.setInput(feats[i]);
        }

        for (List<Node> hiddens : hiddenNodes){
            for (Node hiddenNode : hiddens){
                hiddenNode.calcOutput();
            }
        }
        double[] outs = new double[outputs.size()];

        for (int i = 0; i < outs.length; i++){
            OutputNode node = (OutputNode) outputs.get(i);
            node.calcOutput();
            outs[i] = node.getOutput();
        }
        return outs;
    }

    public List<Node> getInputs() {
        return inputs;
    }

    public List<Node> getOutputs() {
        return outputs;
    }

    public List<List<Node>> getHiddenNodes() {
        return hiddenNodes;
    }
}
