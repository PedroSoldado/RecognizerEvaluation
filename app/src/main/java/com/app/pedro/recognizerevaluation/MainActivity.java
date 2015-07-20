package com.app.pedro.recognizerevaluation;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.Instrumentation;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.app.pedro.puredataplugin.PureData;
import com.app.pedro.puredataplugin.PureDataRecognizer;

import org.puredata.core.PdListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.HashMap;

public class MainActivity extends Activity implements NamingDialog.NamingDialogListener {

    public static PureData pd;
    public static PureDataRecognizer recognizer;
    private TextView loggerView;

    private boolean training;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recognizer = new PureDataRecognizer(getApplicationContext());
        pd = new PureData(getApplicationContext());

        initButtons();

        loggerView = (TextView) findViewById(R.id.textView);
        loggerView.setMovementMethod(new ScrollingMovementMethod());

        createListener();
    }

    private void createListener() {
        final TextView resultLabel = (TextView) findViewById(R.id.resultText);
        pd.getMyDispatcher().addListener("bonk-cooked", new PdListener.Adapter() {
            @Override
            public void receiveList(String source, Object... objects) {
                String bonkOutput;

                int template = (int) Double.parseDouble(objects[0].toString());

                float velocity = Float.parseFloat(objects[1].toString());
                float colorTemperature = Float.parseFloat(objects[2].toString());
                float loudness = Float.parseFloat(objects[3].toString());
                float sigmund = Float.parseFloat(objects[4].toString());

                recognizer.addHit(template, velocity, colorTemperature, loudness);

                bonkOutput = "Template: " + template + "\t\t\t Velocity: " + velocity + "\t\t\t Loudness: " +
                        loudness + "\t\t\t Sigmund~: " + sigmund + "\n";

                if (recognizer.detectHit() != null) {

                    loggerView.setText(loggerView.getText() + bonkOutput);
                    resultLabel.setText(recognizer.getResult(template, velocity));
                }
            }



        });
    }


    @Override
    public void onResume(){
        createListener();
        super.onResume();

    }

    @Override
    public boolean dispatchTouchEvent (MotionEvent event){
        int x = (int)event.getX();
        int y = (int)event.getY();


        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN: recognizer.addTouch(x,y);
        }

        super.dispatchTouchEvent(event);
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        pd.shutDown();
        Log.d("OnDestroy", "Service stopped");
    }

    public void initButtons(){

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

        final Button buttonImport = (Button) findViewById(R.id.importButton);
        buttonImport.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {

                File root = android.os.Environment.getExternalStorageDirectory();
                File dir = new File(root.getAbsolutePath() + getString(R.string.folder));
                File namesFile = new File(dir, getString(R.string.namesFile));
                File templateFile = new File(dir, getString(R.string.templatesFile));
                File intensityFile = new File(dir, getString(R.string.intensityFile));

                boolean importOk = recognizer.importFiles(namesFile,templateFile,intensityFile);
                if(!importOk) {
                    Toast toast = Toast.makeText(getApplicationContext(), "Files don't match (line numbers are differente). Please import correct files.", Toast.LENGTH_SHORT);
                    toast.show();
                }


            }
        });

        final Button buttonLearnOff = (Button) findViewById(R.id.learnOffButton);
        final Button buttonLearnOn = (Button) findViewById(R.id.learnOnButton);
        final Button buttonForget = (Button) findViewById(R.id.forgetButton);
        final Button buttonWrite = (Button) findViewById(R.id.writeButton);
        final Button buttonRead = (Button) findViewById(R.id.readButton);

        buttonLearnOn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                recognizer.startLearning();
                training = true;
                buttonLearnOff.setEnabled(true);
                buttonLearnOn.setEnabled(false);
                Log.e("Click", "learn mode!");
            }
        });

        buttonLearnOff.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                recognizer.stopLearning();
                training = false;

                String[] lineSplit = loggerView.getText().toString().split("\n");
                String[] tokens = lineSplit[lineSplit.length-1].split("\t\t");

                if(tokens[1] != null){
                    int nTemplates = Integer.parseInt(tokens[1]) + 1;
                    recognizer.setTemplatesNumber(nTemplates);
                }

                buttonLearnOff.setEnabled(false);
                buttonLearnOn.setEnabled(true);
                buttonForget.setEnabled(true);
            }
        });

        buttonForget.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                recognizer.forgetTemplate();
                recognizer.setTemplatesNumber(recognizer.getTemplatesNumber() - 1);
                Log.e("Click", "forget!");
            }
        });


        buttonWrite.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                showNamingDialog();
                Log.e("Click", "write!");

            }
        });


        buttonRead.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                recognizer.readTemplates();

                recognizer.setTemplatesNumber(importResultsFileNumbers());
                recognizer.loadNames(importNames());

                buttonForget.setEnabled(true);
                Log.e("Click", "read!");
            }
        });

        final Button buttonIntensity = (Button) findViewById(R.id.intensityActivity);
        buttonIntensity.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(), IntensityActivity.class);
                startActivity(intent);
            }
        });

        final Button buttonPlots = (Button) findViewById(R.id.soundwavesButton);
        buttonPlots.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(), SoundWavesActivity.class);
                startActivity(intent);
            }
        });


    }

    private void showNamingDialog(){

        FragmentManager fm = getFragmentManager();

        NamingDialog dialog = new NamingDialog();
        dialog.show(fm, null);
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {

        Log.e("Writing and saving", "");
        recognizer.writeTemplates();
        exportResultsFile();

    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        // User touched the dialog's negative button
        Log.e("NO LUCK", "NOO");
    }

    private HashMap importNames() {

        File root = android.os.Environment.getExternalStorageDirectory();
        File dir = new File(root.getAbsolutePath() + getString(R.string.folder));
        File file = new File(dir, getString(R.string.namesFile));

        HashMap<Integer, String> nameMap = new HashMap<>();
        BufferedReader reader;
        String line;

        try {

            reader = new BufferedReader(new FileReader(file));

            while ((line = reader.readLine()) != null) {

                String[] splits = line.split("\t");
                int number = Integer.parseInt(splits[0]);

                nameMap.put(number, splits[1]);

            }

            reader.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return nameMap;

    }

    private int importResultsFileNumbers(){

        File root = android.os.Environment.getExternalStorageDirectory();
        File dir = new File(root.getAbsolutePath() + getString(R.string.folder));
        File file = new File(dir, getString(R.string.templatesFile));

        BufferedReader reader;
        int lines = 0;

        try {

            reader = new BufferedReader(new FileReader(file));
            while (reader.readLine() != null)
                lines++;
            reader.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return lines;

    }

    private void exportResultsFile() {

        String fileName = getString(R.string.templatesFile);

        File dir = getFilesDir();
        File file = new File(dir, fileName);

        File root = android.os.Environment.getExternalStorageDirectory();
        File dirOut = new File(root.getAbsolutePath() + getString(R.string.folder));
        dir.mkdirs();
        File fileOut = new File(dirOut, fileName);

        try {

            InputStream in = new FileInputStream(file);
            OutputStream out = new FileOutputStream(fileOut);

            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

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


}
