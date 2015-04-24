package com.app.pedro.recognizerevaluation;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;


public class IntensityActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intensity);


        Button intensityTrain = (Button) findViewById(R.id.intensityButton);
        intensityTrain.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                //recognizer.trainIntensity();
                trainIntensity();
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

    public void trainIntensity(){

        //int nTemplates = recognizer.getTemplates();
        int nTemplates = 2;
        float[] samples = new float[5];

        for(int template = 0; template < nTemplates; template++){




        }
    }
}
