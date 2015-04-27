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

import com.app.pedro.puredataplugin.PureData;
import com.app.pedro.puredataplugin.PureDataRecognizer;

import org.puredata.core.PdListener;

public class MainActivity extends Activity {

    public static PureData pd;
    PureDataRecognizer recognizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        recognizer = new PureDataRecognizer(getApplicationContext());
        pd = new PureData(getApplicationContext());
        //boolean bound = pd.startService();


        initButtons(pd);

        final TextView loggerView = (TextView) findViewById(R.id.textView);
        loggerView.setMovementMethod(new ScrollingMovementMethod());
        final EditText counterBox = (EditText) findViewById(R.id.learnCounter);
        final TextView resultLabel = (TextView) findViewById(R.id.resultText);

        pd.getMyDispatcher().addListener("bonk-cooked", new PdListener.Adapter() {
            @Override
            public void receiveList(String source, Object... objects) {
                String result, bonkOutput;

                int template = (int) Double.parseDouble(objects[0].toString());
                float velocity = Float.parseFloat(objects[1].toString());
                float colorTemperature = Float.parseFloat(objects[2].toString());

                //Intensity
                float hitIntensity = Float.parseFloat(objects[3].toString());


                pd.addHit(template, velocity, colorTemperature, hitIntensity);

                result = pd.getResult(template, velocity);
                bonkOutput = source + "\t\t" + template + "\t\t" + velocity + "\t\t" + hitIntensity + "\n";

                if (pd.detectHit() != null) {

                    if(Integer.parseInt(counterBox.getText().toString()) < 10) {
                        counterBox.setText(""+(Integer.parseInt(counterBox.getText().toString()) + 1));
                    }
                    else counterBox.setText("1");

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

        /*final Button buttonHigh = (Button) findViewById(R.id.highButton);
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
        });*/

        final Button buttonOff = (Button) findViewById(R.id.buttonOff);
        buttonOff.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                pd.sendBang("off");
                //Log.e("Click", "bang: off!");
            }
        });

        final Button buttonOn = (Button) findViewById(R.id.buttonOn);
        buttonOn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                pd.sendBang("on");
                Log.e("Click", "bang: on!");
            }
        });

        final Button buttonLearn = (Button) findViewById(R.id.learnOnButton);
        buttonLearn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                pd.startLearning();
                Log.e("Click", "learn mode!");
            }
        });

        final Button buttonLearnOff = (Button) findViewById(R.id.learnOffButton);
        buttonLearnOff.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                pd.stopLearning();
                Log.e("Click", "learn mode OFF!");
            }
        });

        final Button buttonForget = (Button) findViewById(R.id.forgetButton);
        buttonForget.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                pd.forgetTemplate();
                Log.e("Click", "forget!");
            }
        });

        final Button buttonWrite = (Button) findViewById(R.id.writeButton);
        buttonWrite.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                pd.writeTemplates();
                Log.e("Click", "write!");
            }
        });

        final Button buttonRead = (Button) findViewById(R.id.readButton);
        buttonRead.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                pd.readTemplates();
                Log.e("Click", "read!");
            }
        });

        final Button buttonIntensity = (Button) findViewById(R.id.intensityActivity);
        buttonIntensity.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //pd.shutDown();
                Intent intent = new Intent(getApplicationContext(), IntensityActivity.class);
                startActivity(intent);
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
