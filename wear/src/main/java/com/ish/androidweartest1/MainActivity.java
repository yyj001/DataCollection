package com.ish.androidweartest1;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends WearableActivity implements SensorEventListener{

    private TextView mTextView;
    private TextView mTextViewCount;
    private Button btn;
    private Button btn2;

    private SensorManager sm;
    private double zValue = 0;
    private double preValuex = 0;
    private double preValuey = 0;
    private double preValue = 0;
    private int count = 0;
    private static final String TAG = "sensor";
    //总共采3400个点
    private int colNum = 3400;
    private String s ="";
    //振幅数据
    private double[] data = new double[colNum];
    //按钮判断开始
    private boolean flag = false;

    private int recLen = 0;
    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if(recLen<32){
                recLen++;
                mTextViewCount.setText("" + recLen);
                handler.postDelayed(this, 1000);
            }

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        iniView();
    }

    @Override
    protected void onResume() {
        super.onResume();

        //创建一个SensorManager来获取系统的传感器服务
        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
                ,10000);

    }

    public void iniView()
    {
        btn = (Button)findViewById(R.id.btn);
        btn2 = (Button)findViewById(R.id.btn2);
        mTextView = (TextView) findViewById(R.id.text);
        mTextViewCount = (TextView) findViewById(R.id.text_count);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //停止
                if(flag){
                    flag = false;
                    count = 0;
                    s = "";
                    handler.removeCallbacks(runnable);
                    mTextViewCount.setText("0");
                }else{
                    handler.postDelayed(runnable, 1000);
                    recLen = 0;
                    flag = true;
                    btn.setText("重新开始");
                }
            }
        });
        //提前结束
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                count=0;
                flag = false;
                //输出1000个点
                for(int i = 0;i<5;i++){
                    for(int j=0;j<200;j++){
                        s+="," + formatFloatNumber(data[i*200+j]);
                    }
                    Log.d(TAG, s);
                    s = "";
                }
                handler.removeCallbacks(runnable);
                mTextViewCount.setText("0");
            }
        });
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            //开始接收数据
            if(count<colNum && flag){
                double z = sensorEvent.values[2];
                String zstr = formatFloatNumber(z);
                mTextView.setText(zstr);
                data[count] = z;
                count++;
            }
            //接收完成，输出数据
            else if(count==colNum){
                count=0;
                flag = false;
                //控制台只能一行输出200个左右，分行输出
                for(int i = 0;i<colNum/200;i++){
                    for(int j=0;j<200;j++){
                        s+="," + formatFloatNumber(data[i*200+j]);
                    }
                    Log.d(TAG, s);
                    s = "";
                }
            }//如果flag=fales，还没开始
            else{
                btn.setText("开始");
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onPause() {
        sm.unregisterListener(this);
        super.onPause();
    }

    //指数格式转换
    public static String formatFloatNumber(Double value) {
        if(value != null){
            if(value.doubleValue() != 0.00){
                java.text.DecimalFormat df = new java.text.DecimalFormat("#######0.0000000000000");
                return df.format(value.doubleValue());
            }else{
                return "0.00";
            }
        }
        return "";
    }
}
