package com.app.pedro.recognizerevaluation;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.androidplot.Plot;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.XYStepMode;
import com.app.pedro.puredataplugin.PureData;
import com.app.pedro.puredataplugin.PureDataPlots;

import java.text.DecimalFormat;
import java.util.Observable;
import java.util.Observer;


public class SoundWavesActivity extends Activity {

    private XYPlot plot;
    private MyPlotUpdater plotUpdater;
    Datasource data;
    private Thread myThread;
    private PureDataPlots plots;
    private PureData pd;
    private boolean toggleFlag;
    private SeekBar lowPassBar;
    private SeekBar highPassBar;
    private TextView lowPassText;
    private TextView highPassText;

    private class MyPlotUpdater implements Observer {
        Plot plot;

        public MyPlotUpdater(Plot plot) {
            this.plot = plot;
        }

        @Override
        public void update(Observable o, Object arg) {
            plot.redraw();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sound_waves);

        lowPassBar = (SeekBar) findViewById(R.id.lowPassBar);
        highPassBar = (SeekBar) findViewById(R.id.highPassBar);

        lowPassText = (TextView) findViewById(R.id.lowPassText);
        highPassText = (TextView) findViewById(R.id.highPassText);

        toggleFlag = true;

        plots = new PureDataPlots(getApplicationContext());
        pd = plots.getPdInstance();

        initPlot();
        initButtons();

        lowPassBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                lowPassText.setText(progress + "Hz");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        highPassBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                highPassText.setText(progress + "Hz");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        lowPassText.setText(lowPassBar.getProgress() + " Hz");
        highPassText.setText(highPassBar.getProgress() + " Hz");

    }

    private void initPlot() {
        plot = (XYPlot) findViewById(R.id.soundWavePlot);
        plotUpdater = new MyPlotUpdater(plot);

        data = new Datasource();
        DynamicSeries series = new DynamicSeries(data, 0, "");

        // line,  vertex,  fill
        LineAndPointFormatter series1Format = new LineAndPointFormatter(Color.BLACK, Color.TRANSPARENT, Color.TRANSPARENT, null);


        plot.setRangeBoundaries(0, 1, BoundaryMode.FIXED);
        plot.setRangeStep(XYStepMode.SUBDIVIDE, 5);
        plot.setRangeValueFormat(new DecimalFormat("#.##"));

        plot.getDomainLabelWidget().getLabelPaint().setColor(Color.BLACK);
        plot.getLegendWidget().setVisible(false);


        //Labels colors
        plot.getGraphWidget().getDomainLabelPaint().setColor(Color.BLACK);
        plot.getGraphWidget().getRangeLabelPaint().setColor(Color.BLACK);

        plot.getGraphWidget().getGridBackgroundPaint().setColor(Color.WHITE);
        plot.getGraphWidget().getBackgroundPaint().setColor(Color.WHITE);

        //AXIS COLOR!

        plot.setDomainBoundaries(0, 8000, BoundaryMode.FIXED);
        plot.setDomainValueFormat(new DecimalFormat("#"));
        plot.setDomainStep(XYStepMode.SUBDIVIDE, 17);

        plot.getGraphWidget().getDomainOriginLinePaint().setColor(Color.BLACK);
        plot.getGraphWidget().getRangeOriginLinePaint().setColor(Color.BLACK);

        plot.addSeries(series, series1Format);



        data.addObserver(plotUpdater);
    }

    private void initButtons() {

        Button toggleButton = (Button) findViewById(R.id.toggleButton);
        toggleButton.getBackground().setColorFilter(Color.LTGRAY, PorterDuff.Mode.MULTIPLY);
        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(toggleFlag)
                    pd.sendBang("off");
                else pd.sendBang("on");

                toggleFlag = !toggleFlag;

            }
        });

        Button sendFiltersButton = (Button) findViewById(R.id.sendFiltersButton);
        sendFiltersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int highValue = highPassBar.getProgress();
                int lowValue = lowPassBar.getProgress();

                pd.sendFloat("lowpass", lowValue + "");
                pd.sendFloat("highpass", highValue + "");

            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sound_waves, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up toggleFlag, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        // kick off the data generating thread:
        myThread = new Thread(data);
        myThread.start();
        super.onResume();
    }

    @Override
    public void onPause() {
        data.stopThread();
        super.onPause();
    }

    class Datasource implements Runnable {

        // encapsulates management of the observers watching this datasource for update events:
        class MyObservable extends Observable {
            @Override
            public void notifyObservers() {
                setChanged();
                super.notifyObservers();
            }
        }

        private MyObservable notifier;
        private boolean keepRunning = false;
        private int SAMPLERATE = 44100;
        private int BLOCKSIZE = 1024;
        private int SAMPLE_SIZE = BLOCKSIZE/2 - 1;
        private float RATE_BLOCK_RATIO = (float)SAMPLERATE/BLOCKSIZE;
        float[] dataArray;

        {
            notifier = new MyObservable();
        }

        public void stopThread() {
            keepRunning = false;
        }

        //@Override
        public void run() {
            keepRunning = true;
            while (keepRunning) {

                dataArray = plots.receiveMagnitude();


                notifier.notifyObservers();
            }
        }

        public int getItemCount(int series) {
            return SAMPLE_SIZE;
        }

        public Number getX(int series, int index) {
            if (index >= SAMPLE_SIZE) {
                throw new IllegalArgumentException();
            }

            float frequency = index * 1.0f * RATE_BLOCK_RATIO;

            //Log.e("FREQ", frequency + "");
            return frequency;
        }

        public Number getY(int series, int index) {
            if (index >= SAMPLE_SIZE) {
                throw new IllegalArgumentException();
            }
            return dataArray[index];
        }

        public void addObserver(Observer observer) {
            notifier.addObserver(observer);
        }

        public void removeObserver(Observer observer) {
            notifier.deleteObserver(observer);
        }

    }

    class DynamicSeries implements XYSeries {
        private Datasource datasource;
        private int seriesIndex;
        private String title;

        public DynamicSeries(Datasource datasource, int seriesIndex, String title) {
            this.datasource = datasource;
            this.seriesIndex = seriesIndex;
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }

        @Override
        public int size() {
            return datasource.getItemCount(seriesIndex);
        }

        @Override
        public Number getX(int index) {
            return datasource.getX(seriesIndex, index);
        }

        @Override
        public Number getY(int index) {
            return datasource.getY(seriesIndex, index);
        }
    }
}
