package com.wesley.bpnn.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.wesley.bpnn.net.BPNN;
import com.wesley.bpnn.net.Node;
import com.wesley.bpnn.net.OutputNode;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BPView extends View {
    private BPNN bpnn;
    private Map<Node,RectF> nodeModels = new HashMap<>();
    int w;
    int h;

    int nodeWidth;
    int nodeHeight;

    int parts;
    int partWidth;

    Paint linePaint;
    Paint weightPaint;
    Paint outputPaint;

    public BPView(Context context) {
        super(context);
    }

    public BPView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BPView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setBPNN(BPNN bpnn) {
        this.bpnn = bpnn;
        linePaint = new Paint();
        linePaint.setStrokeWidth(1);
        linePaint.setColor(Color.LTGRAY);

        DashPathEffect effect = new DashPathEffect(new float[]{5,5,5,5},1);
        linePaint.setPathEffect(effect);

        weightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        weightPaint.setStrokeWidth(2);
        weightPaint.setColor(Color.BLACK);
        weightPaint.setTextSize(16);
        weightPaint.setTextAlign(Paint.Align.CENTER);

        outputPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        outputPaint.setStrokeWidth(2);
        outputPaint.setColor(Color.BLACK);
        outputPaint.setTextAlign(Paint.Align.CENTER);
        outputPaint.setTextSize(16);

        parts = 2 + bpnn.getHiddenNodes().size();
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        w = getMeasuredWidth();
        h = getMeasuredHeight();

        nodeWidth = w / 16;
        nodeHeight = h / 16;

        partWidth = w / parts;

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawColor(Color.WHITE);

        for (int i = 0; i < parts; i++) {
            float x = (i + 0.5f) * partWidth;
            canvas.drawLine(x,0,x,h,linePaint);
        }

        int nodePartHeight = h / bpnn.getInputs().size(); //节点的中心
        for (int i = 0; i < bpnn.getInputs().size(); i++){
            float left = partWidth/2 - nodeWidth/2;
            float right = left + nodeWidth;

            float top = (i + 0.5f) * nodePartHeight - nodeHeight/2;
            float bottom = top + nodeHeight;

            RectF rectF = new RectF(left,top,right,bottom);
            Node node = bpnn.getInputs().get(i);
            nodeModels.put(node,rectF);
            canvas.drawRect(rectF,linePaint);
            drawOutput(node,rectF,canvas);
        }


        for (int i = 0; i < bpnn.getHiddenNodes().size(); i++){

            float left = (i + 1)*partWidth + partWidth/2 - nodeWidth/2;
            float right = left + nodeWidth;
            nodePartHeight = h / bpnn.getHiddenNodes().get(i).size();
            for (int j = 0; j < bpnn.getHiddenNodes().get(i).size();j++){

                float top = (j + 0.5f) * nodePartHeight - nodeHeight/2;
                float bottom = top + nodeHeight;

                RectF rectF = new RectF(left,top,right,bottom);
                Node node = bpnn.getHiddenNodes().get(i).get(j);
                nodeModels.put(node,rectF);

                canvas.drawRect(rectF,linePaint);
                drawOutput(node,rectF,canvas);
            }
        }

        nodePartHeight = h / bpnn.getOutputs().size();
        for (int i = 0; i < bpnn.getOutputs().size(); i++){
            float left = w - partWidth/2 - nodeWidth/2;
            float right = left + nodeWidth;

            float top = (i + 0.5f) * nodePartHeight - nodeHeight/2;
            float bottom = top + nodeHeight;

            RectF rectF = new RectF(left,top,right,bottom);
            Node node = bpnn.getOutputs().get(i);
            nodeModels.put(node,rectF);
            canvas.drawRect(rectF,linePaint);
            drawOutput(node,rectF,canvas);
        }



        /*******************  画连接线  *******************/
        for (int i = 0; i < bpnn.getInputs().size(); i++){
            Node startNode = bpnn.getInputs().get(i);
            drawWeight(startNode,canvas);
        }

        for (int i = 0; i < bpnn.getHiddenNodes().size(); i++){
            for (int j = 0; j < bpnn.getHiddenNodes().get(i).size();j++){
                Node startNode = bpnn.getHiddenNodes().get(i).get(j);
                drawWeight(startNode,canvas);
            }
        }

    }

    private void drawWeight(Node startNode,Canvas canvas){
        RectF startRect = nodeModels.get(startNode);

        for (Node endNode : startNode.getChilds().keySet()){
            RectF endRect = nodeModels.get(endNode);
            float startX = startRect.right;
            float startY = startRect.bottom - startRect.height()/2;
            float stopY = endRect.bottom - endRect.height()/2;
            float stopX = endRect.left;
            canvas.drawLine(startX,startY,stopX,stopY, weightPaint);

            double weight = endNode.getWeight(startNode);

            float x = startX + (stopX - startX) *2/3;
            float y = startY + (stopY - startY) *2/3;

            DecimalFormat dFormat=new DecimalFormat("0.0000");
            String weightStr=dFormat.format(weight);
            canvas.drawText("" + weightStr,x,y,weightPaint);
        }
    }

    private void drawOutput(Node node,RectF rectF ,Canvas canvas){
        DecimalFormat dFormat=new DecimalFormat("0.00");
        String outStr=dFormat.format(node.getOutput());

        if (node instanceof OutputNode){
            String expStr=dFormat.format(((OutputNode) node).getExp());
            canvas.drawText( "exp:\n"+expStr,rectF.centerX(),rectF.centerY() + 20,outputPaint);
        }

        canvas.drawText( "out:\n"+outStr,rectF.centerX(),rectF.centerY(),outputPaint);

    }
}
