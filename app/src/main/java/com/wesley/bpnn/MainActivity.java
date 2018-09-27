package com.wesley.bpnn;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.wesley.bpnn.net.BPNN;
import com.wesley.bpnn.view.BPView;
import com.wesley.bpnn.view.ChartUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "BPNN";
    List<Entry> scores = new ArrayList<>();
    LineChart lineChart ;
    BPView bpView;
    BPNN bpnn;
    EditText layersEt;
    EditText rateEt;
    EditText errorEt;
    EditText numberEt;

    RelativeLayout pannelLayout;
    ScrollView settingLayout;

    private double[][] x = {{1,0},{0,1},{1,1},{0,0}};
    private double[][] y = { {1} , {1} , {0} , {0} };
    ScheduledExecutorService scheduledExecutorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

        bpnn  = new BPNN();
        initBPNN();

        bpView = findViewById(R.id.bpView);
        bpView.setBPNN(bpnn);

        lineChart = findViewById(R.id.scoreView);
        ChartUtils.initChart(lineChart);

        layersEt = findViewById(R.id.layers_et);
        rateEt = findViewById(R.id.rate_et);
        errorEt = findViewById(R.id.error_et);
        numberEt = findViewById(R.id.iteration_et);

        settingLayout = findViewById(R.id.setting_layout);
        pannelLayout = findViewById(R.id.pannelLayout);

    }

    private void initBPNN(){
        bpnn.initInputs(2);
        bpnn.addOutputs(1);

    }



    private synchronized void trainWithData(double[] data,double[] exp){
        bpnn.studyOneStep(data,exp);
        bpnn.backPropagation();
        bpView.postInvalidate();
        double score = bpnn.calcLost();
        scores.add(new Entry(bpnn.getStep(), (float) score));
        updateScoreView(new Entry(bpnn.getStep(), (float) score));
    }


    private void updateScoreView(Entry entry){
        ChartUtils.setChartData(lineChart,scores);
        bpView.postInvalidate();

    }


    public void next(View view) {
        int curStep = bpnn.getStep();
        int index = curStep % 4;
        trainWithData(x[index],y[index]);
    }

    public void next50(View view) {
        scheduledExecutorService.schedule(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 50;i ++){
                    int index = i % 4;
                    trainWithData(x[index],y[index]);
                }
            }
        },200, TimeUnit.MILLISECONDS);

    }

    public void train2Target(View view) {

        scheduledExecutorService.schedule(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < bpnn.getThreashholdDepth();i++){
                    int index = i % 4;
                    trainWithData(x[index],y[index]);
                    if (bpnn.getLost() < bpnn.getThreashholdLost()){
                        break;
                    }
                }
            }
        },200, TimeUnit.MILLISECONDS);
    }

    int testIndex = 0;

    public void test(View view) {
        int index =testIndex++ % 4;
        double[] predict = bpnn.predict(x[index]);
        String toast = "输入为： " + x[index][0] + ", " + x[index][1] + ";预测结果为： " + predict[0];
        Toast.makeText(this,toast,Toast.LENGTH_SHORT).show();
    }

    public void cancel(View view) {
        pannelLayout.setVisibility(View.VISIBLE);
        settingLayout.setVisibility(View.GONE);
    }

    public void doInit(View view) {
        bpnn.clear();
        scores.clear();
        initBPNN();
        ChartUtils.clear(lineChart);
        String[] layers = layersEt.getText().toString().trim().split(",");
        for (String n : layers){
            try {
                int num = Integer.parseInt(n);
                bpnn.addHiddenLayer(num);
            }catch (NumberFormatException e){
                e.printStackTrace();
                return;
            }
        }
        bpView.setBPNN(bpnn);
        bpnn.randomWeights();
        bpView.postInvalidate();

        try {
            double rate = Double.parseDouble(rateEt.getText().toString());
            BPNN.studySpeed = rate;
        }catch (NumberFormatException e){
            e.printStackTrace();
            BPNN.studySpeed = 1.0;
        }


        try {
            double errorTarget = Double.parseDouble(errorEt.getText().toString());
            bpnn.setThreashholdLost(errorTarget);
        }catch (NumberFormatException e){
            e.printStackTrace();
            bpnn.setThreashholdLost(0.001);
        }

        try {
            int iterations = Integer.parseInt(numberEt.getText().toString());
            bpnn.setThreashholdDepth(iterations);
        }catch (NumberFormatException e){
            e.printStackTrace();
            bpnn.setThreashholdDepth(Integer.MAX_VALUE);
        }

        pannelLayout.setVisibility(View.VISIBLE);
        settingLayout.setVisibility(View.GONE);

    }

    public void showSetting(View view) {
        pannelLayout.setVisibility(View.GONE);
        settingLayout.setVisibility(View.VISIBLE);
    }

    public void randomWeight(View view) {
        bpnn.clear();
        scores.clear();
        initBPNN();
        String[] layers = layersEt.getText().toString().trim().split(",");
        for (String n : layers){
            try {
                int num = Integer.parseInt(n);
                bpnn.addHiddenLayer(num);
            }catch (NumberFormatException e){
                e.printStackTrace();
                return;
            }
        }
        ChartUtils.clear(lineChart);

        bpnn.randomWeights();
        bpView.postInvalidate();
    }
}
