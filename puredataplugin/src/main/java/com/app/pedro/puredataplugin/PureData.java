package com.app.pedro.puredataplugin;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Point;
import android.os.IBinder;
import android.util.Log;
import android.util.Pair;

import org.puredata.android.io.AudioParameters;
import org.puredata.android.service.PdService;
import org.puredata.android.utils.PdUiDispatcher;
import org.puredata.core.PdBase;
import org.puredata.core.PdListener;
import org.puredata.core.utils.IoUtils;
import org.puredata.core.utils.PdDispatcher;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Pedro on 06-03-2015.
 */
public class PureData {

    private enum TouchType {
        TAP, KNOCK, SLAP, PUNCH, NOTRECOGNIZED
    }

    private List<Hit> hits;

    //Need to have a list?? -> Only a single object
    private List<Touch> touches;
    private InputStream stream;
    private Context appContext;
    private int handlePatch;
    private PdService pdService;
    private File saveFile;
    private PdDispatcher myDispatcher;
    private ServiceConnection pdConnection;
    private PdListener bonkListener;


    public PureData(Context c) {

        try {
            myDispatcher = new PdUiDispatcher() {
                //@Override
                //public void print(String s) { Log.e("Pd print", s);}
            };

            hits = new ArrayList<>();
            touches = new ArrayList<>();
            handlePatch = 0;
            pdService = null;
            stream = c.getResources().getAssets().open("test.zip");
            appContext = c;
            saveFile = c.getFilesDir();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Creates a new service connection to Pure Data and binds the service (background running service)
    public boolean startService() {

        //Create connection and how it behaves on connection accepted
        pdConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                pdService = ((PdService.PdBinder)service).getService();
                initPd();

            }
            @Override
            public void onServiceDisconnected(ComponentName name) {
            /*Never called*/
            }


        };

        boolean bound = appContext.bindService(new Intent(appContext, PdService.class), pdConnection, appContext.BIND_AUTO_CREATE);

        return bound;

    }

    private void loadPatch() { /* Needed to clean code up maybe??*/}



    public void initPd() {

        PdBase.setReceiver(myDispatcher);
        //myDispatcher.addListener("GUI", new PDListener());
        //myDispatcher.addListener("bonk-cooked", new PDListener());
        /*myDispatcher.addListener("bonk-cooked", new PdListener.Adapter() {
            @Override
            public void receiveList(String source, Object... objects) {
                String result, bonkOutput;

                int template = (int) Double.parseDouble(objects[0].toString());
                float velocity = Float.parseFloat(objects[1].toString());
                float colorTemperature = Float.parseFloat(objects[2].toString());

                addHit(template,velocity, colorTemperature);

                if(detectHit() != null){

                    //output.add(bonkOutput);

                    //sb.append(source + " " + template + " " + velocity + " " + colorTemperature + "\n");
                    //logTextView.setText(sb.toString());
                }

                //resultLabel.setText("IDLE");
            }
        });*/
        startAudio();

    }

    private List sendResult(List result) {

        return result;
    }


    private void startAudio() {

        if(pdService == null) { Log.e("pdserviceNull", "no"); return;}

        if (!initAudio(2, 2) && !initAudio(1, 2)) {  /* see below */
            if (!initAudio(0, 2)) {
                Log.e("PdTag", "Unable to initialize audio interface");
                return;
            } else {
                Log.w("PdTag", "No audio input available");
            }
        }

        if(handlePatch == 0) {

            try{
                IoUtils.extractZipResource(stream, saveFile, true);
                String path = saveFile.getPath();
                handlePatch = PdBase.openPatch(new File(path, "test.pd"));

                //Log.e("Handle Patch", Integer.toString(handlePatch));
            } catch (IOException e) {
                Log.e("PdTag openPatch", e.toString());
                return;
            }

            pdService.startAudio();

        }

    }

    private boolean initAudio(int nIn, int nOut) {

        int sampleRate = AudioParameters.suggestSampleRate();

        try {
            pdService.initAudio(sampleRate, nIn, nOut, -1);
            /* negative values default to PdService preferences */
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    private void stopAudio() {

        if (pdService == null) return;
        /* consider ramping down the volume here to avoid clicks */
        pdService.stopAudio();

    }




    public void sendFloat(String receiver, String number) {
        float value = Float.parseFloat(number);
        PdBase.sendFloat(receiver, value);
    }

    public void sendBang(String receiver){

        Log.e("BANG", receiver);
        PdBase.sendBang(receiver);
    }

    public void writeTemplates(String receiver) {
        sendBang(receiver);
    }

    public void startLearning(String receiver){
        sendBang(receiver);
    }

    public void stopLearning(String receiver){
        sendBang(receiver);
    }

    public void forgetTemplate(String receiver){
        sendBang(receiver);
    }

    public void readTemplates(String receiver) {
        sendBang(receiver);
    }

    public void shutDown() {
        stopAudio();

        if(handlePatch != 0) {
            PdBase.closePatch(handlePatch);
            handlePatch = 0;

        }

        myDispatcher.release();
        PdBase.release();

        try {
            pdService.unbindService(pdConnection);
        }
        catch (IllegalArgumentException e) {
            pdService = null;
        }
    }

    public String debugLog(String logLine) {

        return logLine;

    }

    public void addTouch(int x, int y) {

        Touch touch = new Touch(x,y);
        touches.add(touch);
    }

    public void addHit(float instrument, float velocity, float color) {

        Hit hit = new Hit(instrument,velocity,color);

        if(hits.size() > 9)
            hits.clear();

        hits.add(hit);
    }

    //Detect a hit on the screen, by comparing the touch with recorder hit timestamps
    public Gesture detectHit() {

        Gesture gesture;

        Log.e("TOUCHES", "" + (touches.size()-1));
        Log.e("HITS", "" + (hits.size()-1));

        if(touches.isEmpty()) {
            return null;
        }


        Touch t = touches.remove(touches.size() - 1);
        Hit h = hits.remove(hits.size()-1);

        long touchTs = t.getTimestamp();
        long hitTs = h.getTimestamp();


        //if(t.getTimestamp() == h.getTimestamp() + 200) {
        if(hitTs <= touchTs + 200 && hitTs >= touchTs ){

            Log.e("Detected Hit!", "At " + hitTs + " " + touchTs);
            gesture = new Gesture(t, h);
            return gesture;
        }

        return null;




    }


    public void debugTouchList() {
        Log.e("Size of Touch List", "" + touches.size());
        for(Touch t: touches) {
            Log.e("Coordinates:", "X:" + t.getLocation().first + "Y: " + t.getLocation().second);
        }
    }

    public void debugHitList(){

        Log.e("Size of Hit List", "" + hits.size());
        /*for(Hit h: hits) {
            Log.e("Hit: ", " " + h.getTemplate() + " " + h.getIntensity() + " " + h.getTimestamp());
        }*/
    }

    public String getResult(int template, float  velocity) {

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

    public PdDispatcher getMyDispatcher(){
        return myDispatcher;
    }


}
