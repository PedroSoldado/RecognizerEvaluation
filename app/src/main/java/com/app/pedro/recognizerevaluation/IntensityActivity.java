package com.app.pedro.recognizerevaluation;

import android.content.Context;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.app.pedro.puredataplugin.Gesture;
import com.app.pedro.puredataplugin.PureData;

import org.puredata.core.PdListener;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;


public class IntensityActivity extends ActionBarActivity {

    private Thread thread;
    List <Gesture> gestureList;
    boolean training;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intensity);

        training = false;
        gestureList = new ArrayList<>();


        View.OnClickListener clickListener =  new View.OnClickListener() {

            @Override
            public void onClick(final View v) {
                //trainT0.setTag(0);
                //recognizer.trainIntensity();
                training = true;

                thread =  new Thread(){
                    @Override
                    public void run(){
                        try {
                            synchronized(this){
                                wait();
                            }
                        }
                        catch(InterruptedException ex){
                        }
                        Log.e("WAKE", "");
                        trainIntensity((int)v.getTag());
                    }
                };

                thread.start();

            }
        };


        Button trainT0 = (Button) findViewById(R.id.buttonT0);
        trainT0.setTag(0);
        trainT0.setOnClickListener(clickListener);

        Button trainT1 = (Button) findViewById(R.id.buttonT1);
        trainT1.setTag(1);
        trainT1.setOnClickListener(clickListener);



        MainActivity.pd.getMyDispatcher().addListener("bonk-cooked", new PdListener.Adapter() {
            @Override
            public void receiveList(String source, Object... objects) {
                String result, bonkOutput;

                int template = (int) Double.parseDouble(objects[0].toString());
                float velocity = Float.parseFloat(objects[1].toString());
                float colorTemperature = Float.parseFloat(objects[2].toString());

                //Intensity
                float hitIntensity = Float.parseFloat(objects[3].toString());
                //Log.e("INT", hitIntensity+"");

                MainActivity.pd.addHit(template, velocity, colorTemperature, hitIntensity);


                Gesture g = MainActivity.pd.detectHit();

                Log.e("TRAINING", training + "");
                if (g != null && training) {

                    gestureList.add(g);
                    Log.e("SIZE", gestureList.size()+"");
                }

                if(gestureList.size() == 15)
                {
                    synchronized(thread){
                        thread.notifyAll();
                    }
                }

            }

        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_intensity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void trainIntensity(int tag){

        Log.e("YEEEEEEEEEEEEEEEES", gestureList.size()+"");
        //int nTemplates = recognizer.getTemplates();
        int nTemplates = 2;
        List<Float> samples = new ArrayList<Float>();

        float sum = 0;


        String weak, medium, strong;
        weak = "t" + tag + "_" + "weak";
        medium = "t" + tag + "_" + "medium";
        strong = "t" + tag + "_" + "strong";

        int idWeak = getResources().getIdentifier(weak,"id", this.getPackageName());
        int idMedium = getResources().getIdentifier(medium,"id", this.getPackageName());
        int idStrong = getResources().getIdentifier(strong,"id", this.getPackageName());

        final TextView weakT = (TextView) findViewById(idWeak);
        final TextView mediumT = (TextView) findViewById(idMedium);
        final TextView strongT = (TextView) findViewById(idStrong);


        for(int i = 0; i < 15; i += 5) {

            for (Gesture ges : gestureList.subList(i, i + 4)) {
                sum += ges.getIntensity();
            }

            final float average = sum / 5;
            //gestureList.clear();

            switch(i) {

                case 0:
                    runOnUiThread(new Runnable() {
                    public void run() {
                        weakT.setText(average + "");
                    }
                });
                case 5:
                    runOnUiThread(new Runnable() {
                        public void run() {
                            mediumT.setText(average + "");
                        }
                    });
                    break;
                case 10:
                    runOnUiThread(new Runnable() {
                        public void run() {
                            strongT.setText(average + "");
                        }
                    });

            }

        }

        gestureList.clear();
        training = false;



        //writeResults(average+" ");
        //weakT.setText(average + "");





    }


    public boolean dispatchTouchEvent (MotionEvent event){
        int x = (int)event.getX();
        int y = (int)event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //Log.e("TOUCH Dispatch! on ", " " + x + y);
                MainActivity.pd.addTouch(x, y);

                break;
        }
        super.dispatchTouchEvent(event);
        return true;
    }

    private void writeResults(String s) {

        try {

            OutputStreamWriter out = new OutputStreamWriter(openFileOutput("intensityResults.txt", Context.MODE_APPEND));

            out.write("NO");
            out.write(s);

            out.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
