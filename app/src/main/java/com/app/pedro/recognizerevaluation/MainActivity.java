package com.app.pedro.recognizerevaluation;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.app.pedro.puredataplugin.PdPluginListener;
import com.app.pedro.puredataplugin.PureData;

import org.puredata.core.PdListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    PureData pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final List<String> log = new ArrayList<>();
        final String logString = "";

        pd = new PureData(getApplicationContext());
        boolean bound = pd.startService();


        initButtons(pd);

        final TextView loggerView = (TextView) findViewById(R.id.textView);
        loggerView.setMovementMethod(new ScrollingMovementMethod());

        final TextView resultLabel = (TextView) findViewById(R.id.resultText);

        //pd.getMyDispatcher().addListener("bonk-cooked", new PdPluginListener());
        pd.getMyDispatcher().addListener("bonk-cooked", new PdListener.Adapter() {
            @Override
            public void receiveList(String source, Object... objects) {
                String result, bonkOutput;

                int template = (int) Double.parseDouble(objects[0].toString());
                float velocity = Float.parseFloat(objects[1].toString());
                float colorTemperature = Float.parseFloat(objects[2].toString());

                pd.addHit(template, velocity, colorTemperature);

                result = getResult(template, velocity);
                bonkOutput = source + " " + template + " " + velocity + " " + colorTemperature + "\n";

                if (pd.detectHit() != null) {

                    loggerView.setText(loggerView.getText() + bonkOutput);
                    resultLabel.setText(result);
                }
                //resultLabel.setText("IDLE");

            }

        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    @Override
    public boolean dispatchTouchEvent (MotionEvent event){
        int x = (int)event.getX();
        int y = (int)event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //Log.e("TOUCH Dispatch! on ", " " + x + y);
                pd.addTouch(x, y);

                break;
        }
        super.dispatchTouchEvent(event);
        return true;
    }

   /* @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int)event.getX();
        int y = (int)event.getY();
        Log.e("TOUCH! on ", " " + x + y);
        pd.addTouch(x,y);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.e("TOUCH DOWN!", " " + x + y);
                break;
            case MotionEvent.ACTION_MOVE:
                break;

            case MotionEvent.ACTION_UP:
                Log.e("TOUCH UP!", "YES");
                break;
        }

        return true;
    }*/

    @Override
    public void onDestroy() {
        super.onDestroy();
        pd.shutDown();
        Log.d("OnDestroy", "Service stopped");
    }

    public void initButtons(final PureData pd){

        final Button buttonHigh = (Button) findViewById(R.id.highButton);
        final EditText hpValue = (EditText) findViewById(R.id.hpNumber);

        buttonHigh.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                pd.sendFloat("highpass", hpValue.getText().toString());
                Log.e("Click", "bang: off!");
            }
        });

        final Button buttonLow = (Button) findViewById(R.id.lowButton);
        final EditText lpValue = (EditText) findViewById(R.id.lpNumber);

        buttonLow.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                pd.sendFloat("highpass", lpValue.getText().toString());
                Log.e("Click", "bang: off!");
            }
        });

        final Button buttonOff = (Button) findViewById(R.id.buttonOff);
        buttonOff.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                pd.sendBang("off");
                //Log.e("Click", "bang: off!");
                pd.debugTouchList();
            }
        });

        final Button buttonOn = (Button) findViewById(R.id.buttonOn);
        buttonOn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                pd.sendBang("on");
                Log.e("Click", "bang: on!");
                pd.debugHitList();
            }
        });

        final Button buttonLearn = (Button) findViewById(R.id.learnButton);
        buttonLearn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                pd.startLearning("learnOn");
                Log.e("Click", "learn mode!");
            }
        });

        final Button buttonLearnOff = (Button) findViewById(R.id.learnOffButton);
        buttonLearnOff.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                pd.stopLearning("learnOff");
                Log.e("Click", "learn mode OFF!");
            }
        });

        final Button buttonForget = (Button) findViewById(R.id.forgetButton);
        buttonForget.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                pd.forgetTemplate("forgetTemplate");
                Log.e("Click", "forget!");
            }
        });

        final Button buttonWrite = (Button) findViewById(R.id.writeButton);
        buttonWrite.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                pd.writeTemplates("writeTemplates");
                Log.e("Click", "write!");
            }
        });

        final Button buttonRead = (Button) findViewById(R.id.readButton);
        buttonRead.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                pd.readTemplates("readTemplates");
                Log.e("Click", "read!");
            }
        });
    }

    private String getResult(int template, float  velocity) {

        String result;

        switch(template){
            case 0: result = "TAP"; break;
            case 1: result = "KNOCK"; break;
            case 2: result = "SLAP"; break;
            case 3: result = "PUNCH"; break;
            default: result = "NOT RECOGNIZED";
        }

        result += " " + velocity;

        return result;
    }

}
