package com.app.pedro.recognizerevaluation;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.app.pedro.puredataplugin.Gesture;

import org.puredata.core.PdListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class IntensityActivity extends ActionBarActivity {

    private Thread thread;
    private List<Gesture> gestureList;
    private boolean training;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intensity);

        training = false;
        gestureList = new ArrayList<>();

        buildTrainer(MainActivity.recognizer.getTemplatesNumber());

        Button importFromFile = (Button) findViewById(R.id.importButton);
        importFromFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                importIntensityResults();
            }
        });

        Button saveFile = (Button) findViewById(R.id.saveFileButton);
        saveFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.e("AQUI", "");
                saveFile();
            }

        });

        createListener();
    }

    private void createListener() {
        MainActivity.pd.getMyDispatcher().addListener("bonk-cooked", new PdListener.Adapter() {
            @Override
            public void receiveList(String source, Object... objects) {

                int template = (int) Double.parseDouble(objects[0].toString());
                float velocity = Float.parseFloat(objects[1].toString());
                float colorTemperature = Float.parseFloat(objects[2].toString());
                float vsnapValue = Float.parseFloat(objects[3].toString());

                MainActivity.recognizer.addHit(template, velocity, colorTemperature, vsnapValue);

                Gesture g = MainActivity.recognizer.detectHit();

                if (g != null) {

                    if (training) {
                        gestureList.add(g);
                        Log.e("SIZE", gestureList.size() + "");
                    }

                    TextView resultLabel = (TextView) findViewById(R.id.resultText);
                    String result = MainActivity.recognizer.getResult(template, velocity);

                    resultLabel.setText(result);
                }

                if (gestureList.size() == 15) {
                    synchronized (thread) {
                        thread.notifyAll();
                    }
                }

            }

        });

    }

    @Override
    public void onResume(){
        super.onResume();
        createListener();
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

    //Train intensity for the given template
    public void trainIntensity(int template) {

        Log.e("TRAINING NOW", gestureList.size() + "");
        //int nTemplates = recognizer.getTemplates();

        float sum = 0;

        final TextView weakT = (TextView) findViewById(template*100 + 0);
        final TextView mediumT = (TextView) findViewById(template * 100 + 1);
        final TextView strongT = (TextView) findViewById(template * 100 + 2);

        for (int i = 0; i < 15; i += 5) {

            for (Gesture ges : gestureList.subList(i, i + 4)) {
                sum += ges.getIntensity();
            }

            final float average = sum / 5;
            sum = 0;

            switch (i) {

                case 0:
                    runOnUiThread(new Runnable() {
                        public void run() {
                            weakT.setText(average + "");
                        }
                    });
                    break;
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
                    break;
            }

        }

        gestureList.clear();
        training = false;
    }


    public boolean dispatchTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:

                MainActivity.recognizer.addTouch(x, y);

                break;
        }
        super.dispatchTouchEvent(event);
        return true;
    }

    private void saveFile(){

        TableLayout table = (TableLayout) findViewById(R.id.table);
        List<String> results = new ArrayList<>();


        String write;

        for(int i=0; i < table.getChildCount(); i++)
        {
            //0 is header row
            View view = table.getChildAt(i+1);

            if (view instanceof TableRow) {

                TableRow row = (TableRow) view;

                write = i + " ";

                loop:
                for(int x=1 ; x < 4; x++) {

                    View v = row.getChildAt(x);

                    if(v instanceof TextView){
                        TextView text = (TextView) v;

                        if(text.getText().equals("NA")) {
                            write = "";
                            break loop;
                        }


                        write += text.getText() + " ";
                    }

                }

                Log.e("AQUII", write);
                results.add(write);




            }
        }

        Log.e("HGERE", "SSDDS");

        writeResults(results);
    }

    /**
     * Import previous intensity training for all classes on the file
     */
    private void importIntensityResults(){

        File root = android.os.Environment.getExternalStorageDirectory();
        File dir = new File(root.getAbsolutePath() + getString(R.string.folder));
        File file = new File(dir, getString(R.string.intensityFile));

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {

            String line;
            String[] result;
            List<Float> resultFloats = new ArrayList<>();

            int template;

            while ((line = br.readLine()) != null) {

                result = line.split(" ");

                template = Integer.parseInt(result[0]);
                TableRow row = getTableRow(template);
                if(row == null)
                    return;

                for(int i = 1; i < result.length; i++){

                    float res = Float.parseFloat(result[i]);
                    resultFloats.add(res);

                }

                TextView weakT = (TextView) row.getChildAt(1);
                TextView mediumT = (TextView) row.getChildAt(2);
                TextView strongT = (TextView) row.getChildAt(3);

                weakT.setText(resultFloats.get(0).toString());
                mediumT.setText(resultFloats.get(1).toString());
                strongT.setText(resultFloats.get(2).toString());

                MainActivity.recognizer.loadIntensities(template, resultFloats);

                resultFloats.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private TableRow getTableRow(int template){

        TableLayout table = (TableLayout) findViewById(R.id.table);

        for(int i = 1 ; i < table.getChildCount() + 1 ; i++) {
            View view = table.getChildAt(i);

            if(view == null)
                return null;

            if (view instanceof TableRow) {

                TableRow row = (TableRow) view;

                if((int)row.getTag() == template){
                    Log.e("RIGHT", ((TextView)row.getChildAt(0)).getText().toString());
                    return row;
                }
            }
        }

        return null;
    }

    private void writeResults(List<String> results) {

        File root = android.os.Environment.getExternalStorageDirectory();
        File dir = new File(root.getAbsolutePath() + getString(R.string.folder));
        dir.mkdirs();
        File file = new File(dir, getString(R.string.intensityFile));

        try {
            FileOutputStream f = new FileOutputStream(file, false);
            PrintWriter pw = new PrintWriter(f);

            for(String s : results) {
                if(s.equals(""))
                    continue;
                pw.append(s + "\n");
                pw.flush();
            }
            pw.close();
            f.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void buildTrainer(int numberOfClasses){

        HashMap<Integer, String> nameMap = MainActivity.recognizer.getNames();

        LayoutInflater li = LayoutInflater.from(this);
        TableLayout t = (TableLayout) findViewById(R.id.table);

        TextView weakTextbox, mediumTextbox, strongTextbox;

        for(int i=0; i < numberOfClasses; i++){

            TableRow tr = (TableRow) li.inflate(R.layout.intensitytablerow, null);

            TextView classNumber = (TextView) tr.getChildAt(0);
            classNumber.setText(i + "\t" + nameMap.get(i));

            weakTextbox = (TextView) tr.getChildAt(1);
            mediumTextbox = (TextView) tr.getChildAt(2);
            strongTextbox = (TextView) tr.getChildAt(3);

            weakTextbox.setId(i * 100 + 0);
            mediumTextbox.setId(i * 100 + 1);
            strongTextbox.setId(i * 100 + 2);

            Button trainButton = (Button) tr.getChildAt(4);

            trainButton.setTag(i);
            trainButton.setOnClickListener(getClickListener());

            tr.setTag(i);
            t.addView(tr);
        }
    }

    private View.OnClickListener getClickListener(){

        View.OnClickListener clickListener = new View.OnClickListener() {

            @Override
            public void onClick(final View v) {
                training = true;

                thread = new Thread() {
                    @Override
                    public void run() {
                        try {
                            synchronized (this) {
                                wait();
                            }
                        } catch (InterruptedException ex) {
                        }
                        Log.e("WAKE", "");
                        trainIntensity((int) v.getTag());
                    }
                };

                thread.start();

            }
        };

        return clickListener;
    }



}
