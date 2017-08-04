/**A tomato clock for Android by fengh16.
 * --Version 0.1 on 17.8.3.
 * Contact me via fengh16@163.com
 */

package com.koala_fh.tomatoclock;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private int minPerClock = 25, restPerClock = 5; //Setting the minutes per Clock and Rest.
    private int nowTomatoNum;                       //Using for many tomatos.
    private int totalTomatoNum;
    public final int GoInterval = 100;              //Using for change onTick interval (ms).
    private Boolean isStart = true;                 //Using for determining whether the
    private int logTotalNum = 0, logTotalMin = 0, logTotalRest = 0;
    private Set<String> logsOfTomatos;              //Three int and the set are used for logs.
    private SharedPreferences mySettings;
    private SharedPreferences.Editor settingsEditer;//For saving settings.
    private int workColor = 0xFF3F51B5, restColor = Color.GREEN;    //The color of progressbars.

    private TextView timeshow, infoshow, workingshow, aimhint, clocknumshow, workminshow,
            restminshow;
    private EditText aimIn, clockIn, workminIn, restminIn;
    private ProgressBar nowprogress, loadingprogress, restingprogress;
    private Button mbutton;

    //A function converting the clock num to String.
    private String ZeroBefore(int inputInt){
        if (inputInt < 10)
            return "0" + inputInt;
        else
            return Integer.toString(inputInt);
    }

    //Function to return the time String.
    private String getNowTime(){
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
    }

    public class MyCountDownTimer extends CountDownTimer{
        private boolean working;        // The working mode or the resting mode.
        private boolean haveCanceled;   // For that even if I called cancel(), onFinish is still
                                        // called, I added the boolean haveCanceled, using it to
                                        // know if the CountDownTimer have been canceled.
        MyCountDownTimer(long millisInFuture, long countDownInterval, boolean mworking){
            super(millisInFuture, countDownInterval);
            haveCanceled = false;
            working = mworking;
            nowprogress.setMax((int)(millisInFuture / GoInterval));
            nowprogress.setProgress(0);
            if(working){
                infoshow.setText("  Working... " + nowTomatoNum + "/" + totalTomatoNum);
                timeshow.setText(ZeroBefore(minPerClock) + ":00");
            }
            else{
                infoshow.setText("  Resting... " + nowTomatoNum + "/" + (totalTomatoNum - 1));
                timeshow.setText(ZeroBefore(restPerClock) + ":00");
            }
        }

        // Function to show that the CountDownTimer have been canceled.
        public void setHaveCanceled(){
            haveCanceled = true;
        }

        @Override
        public void onTick(long millisUntilFinished){
            if(!haveCanceled) {
                long time = millisUntilFinished / 1000;
                int minute = (int) (time / 60);
                int second = (int) (time % 60);
                nowprogress.setProgress((int) (millisUntilFinished / GoInterval));
                timeshow.setText(ZeroBefore(minute) + ":" + ZeroBefore(second));
            }
        }

        @Override
        public void onFinish(){
            if(!haveCanceled) {
                if (working && nowTomatoNum < totalTomatoNum) {
                    RestingViewShow();
                    logTotalRest += restPerClock;
                    settingsEditer.putInt("TotalRest", logTotalRest);
                    logsOfTomatos.add("<Worked><Time>" + getNowTime() + "</Time></Worked>");
                    settingsEditer.putStringSet("Logs", logsOfTomatos);
                    settingsEditer.commit();
                    RestAgain();
                } else if (!working && nowTomatoNum < totalTomatoNum) {
                    nowTomatoNum++;
                    WorkingViewShow();
                    logTotalNum++;
                    logTotalMin += minPerClock;
                    settingsEditer.putInt("TotalNum", logTotalNum);
                    settingsEditer.putInt("TotalMin", logTotalMin);
                    logsOfTomatos.add("<Rested><Time>" + getNowTime() + "</Time></Rested>");
                    settingsEditer.putStringSet("Logs", logsOfTomatos);
                    settingsEditer.commit();
                    Toast toast = Toast.makeText(getApplicationContext(), "Your total tomato num: "
                            + mySettings.getInt("TotalNum", 0), Toast.LENGTH_LONG);
                    toast.show();
                    WorkAgain();
                } else {
                    StopSet();
                }
            }
        }
    }

    private MyCountDownTimer mcout;

    // Show the main page.
    void NoStartingVisible(){
        loadingprogress.setVisibility(View.INVISIBLE);
        restingprogress.setVisibility(View.INVISIBLE);
        timeshow.setVisibility(View.INVISIBLE);
        workingshow.setVisibility(View.INVISIBLE);
        nowprogress.setVisibility(View.INVISIBLE);
        aimIn.setVisibility(View.VISIBLE);
        aimhint.setVisibility(View.VISIBLE);
        clocknumshow.setVisibility(View.VISIBLE);
        clockIn.setVisibility(View.VISIBLE);
        workminshow.setVisibility(View.VISIBLE);
        workminIn.setVisibility(View.VISIBLE);
        restminshow.setVisibility(View.VISIBLE);
        restminIn.setVisibility(View.VISIBLE);
    }

    // Show the CountDown page.
    void StartedVisible(){
        loadingprogress.setVisibility(View.VISIBLE);
        restingprogress.setVisibility(View.INVISIBLE);  // Used for resting, INVISIBLE now.
        timeshow.setVisibility(View.VISIBLE);
        workingshow.setVisibility(View.VISIBLE);
        nowprogress.setVisibility(View.VISIBLE);
        aimIn.setVisibility(View.INVISIBLE);
        aimhint.setVisibility(View.INVISIBLE);
        clocknumshow.setVisibility(View.INVISIBLE);
        clockIn.setVisibility(View.INVISIBLE);
        workminshow.setVisibility(View.INVISIBLE);
        workminIn.setVisibility(View.INVISIBLE);
        restminshow.setVisibility(View.INVISIBLE);
        restminIn.setVisibility(View.INVISIBLE);
    }

    // Working View settings.
    void WorkingViewShow(){
        loadingprogress.setVisibility(View.VISIBLE);
        restingprogress.setVisibility(View.INVISIBLE);
        nowprogress.getProgressDrawable().setColorFilter(workColor, PorterDuff.Mode.SRC_IN);
        // Set Color.
        nowprogress.setRotation(0);
//        timeshow.setTextColor(workColor);
//        workingshow.setTextColor(workColor);
    }

    // Resting View settings.
    void RestingViewShow(){
        loadingprogress.setVisibility(View.INVISIBLE);
        restingprogress.setVisibility(View.VISIBLE);
        nowprogress.getProgressDrawable().setColorFilter(restColor, PorterDuff.Mode.SRC_IN);
        // Set Color.
        nowprogress.setRotation(180);
//        timeshow.setTextColor(restColor);
//        workingshow.setTextColor(restColor);
    }

    // Some settings for clicking Start button.
    void StartSet(){
        isStart = false;
        mbutton.setText("STOP");
        nowprogress.setProgress(nowprogress.getMax());
        StartedVisible();
        WorkingViewShow();
        workingshow.setText(aimIn.getText());
        nowTomatoNum = 1;
        totalTomatoNum = Integer.valueOf(clockIn.getText().toString());
        minPerClock = Integer.valueOf(workminIn.getText().toString());
        restPerClock = Integer.valueOf(restminIn.getText().toString());
    }

    // Called when Stopping the CountDownTimer.
    void StopSet(){
        mcout.setHaveCanceled();
        mcout.cancel();
        isStart = true;
        mbutton.setText("START");
        nowprogress.setProgress(nowprogress.getMax());
        NoStartingVisible();
        infoshow.setText("About to Start!");
    }

    // Use it outside of MyCountDownTimer.
    void RestAgain(){
        mcout = new MyCountDownTimer(restPerClock * 60 * 1000, GoInterval, false);
        mcout.start();
    }

    void WorkAgain(){
        mcout = new MyCountDownTimer(minPerClock * 60 * 1000, GoInterval, true);
        mcout.start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // Screen orientation.

        infoshow = (TextView)findViewById(R.id.stateInfoShow);
        timeshow = (TextView)findViewById(R.id.timeshow);
        nowprogress = (ProgressBar)findViewById(R.id.progressBar);
        mbutton = (Button)findViewById(R.id.button);
        workingshow = (TextView)findViewById(R.id.workingAimShow);
        nowprogress.setProgress(nowprogress.getMax());
        loadingprogress = (ProgressBar)findViewById(R.id.progressBar0);
        restingprogress = (ProgressBar)findViewById(R.id.progressBar1);
        aimIn = (EditText)findViewById(R.id.aimInputing);
        aimhint = (TextView)findViewById(R.id.clockinputshow);
        clockIn = (EditText)findViewById(R.id.aimnumInputing);
        clocknumshow = (TextView)findViewById(R.id.clocknuminputshow);
        workminshow = (TextView)findViewById(R.id.workinginputshow);
        restminshow = (TextView)findViewById(R.id.restinginputshow);
        workminIn = (EditText)findViewById(R.id.workingnumInputing);
        restminIn = (EditText)findViewById(R.id.restingnumInputing);

        // Load the settings.
        mySettings = getSharedPreferences("Settings_Tomato", Context.MODE_APPEND);
        settingsEditer = mySettings.edit();
        logTotalNum = mySettings.getInt("TotalNum", 0);
        logTotalMin = mySettings.getInt("TotalMin", 0);
        logTotalRest = mySettings.getInt("TotalRest", 0);
        logsOfTomatos = mySettings.getStringSet("Logs", new HashSet<String>());
        int h;
        Log.d("5", logsOfTomatos.toString());

        // Show the things needed.
        NoStartingVisible();

        // Set the round progressbar (working) red and (resting) green.
        loadingprogress.getIndeterminateDrawable().setColorFilter(workColor,
                PorterDuff.Mode.MULTIPLY);
        restingprogress.getIndeterminateDrawable().setColorFilter(restColor,
                PorterDuff.Mode.MULTIPLY);
        restingprogress.setRotationX(180);  // Make it rotate reverse.

        mbutton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(isStart){
                    // If clicking means to start.
                    StartSet();
                    mcout = new MyCountDownTimer(minPerClock * 60 * 1000, GoInterval, true);
                    mcout.start();
                    logsOfTomatos.add("<Start><Time>" + getNowTime() + "</Time><Text>" +
                            aimIn.getText().toString() + "</Text></Start>");
                    settingsEditer.putStringSet("Logs", logsOfTomatos);
                    settingsEditer.commit();
                }
                else{
                    StopSet();
                    logsOfTomatos.add("<Stop><Time>" + getNowTime() + "</Time><Text>" +
                            aimIn.getText().toString() + "</Text></Stop>");
                    settingsEditer.putStringSet("Logs", logsOfTomatos);
                    settingsEditer.commit();
                }
            }
        });
    }
}
