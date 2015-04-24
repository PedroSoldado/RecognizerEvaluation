package com.app.pedro.puredataplugin;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Pedro on 23-04-2015.
 */
public class PureDataRecognizer {

    private PureData pd;
    private List<Hit> hits;
    //Need to have a list?? -> Only a single object
    private List<Touch> touches;
    //Map the template to a list of 3 floats - 3 levels of intensity (weak, medium, strong)
    private HashMap<Integer, List<Float>> intensityMap;
    private boolean learning;

    private int templatesLearned;


    public PureDataRecognizer(Context context) {

        pd = new PureData(context);

        templatesLearned = 0;
        hits = new ArrayList<>();
        touches = new ArrayList<>();
        intensityMap = new HashMap<>();
        learning = false;

    }


    public void addTouch(int x, int y) {

        Touch touch = new Touch(x,y);
        touches.add(touch);
    }

    public void addHit(float instrument, float velocity, float color, float intensity) {

        Hit hit = new Hit(instrument,velocity,color, intensity);

        if(hits.size() > 9)
            hits.clear();

        hits.add(hit);
    }

    //Detect a hit on the screen, by comparing the touch with recorder hit timestamps
    public Gesture detectHit() {

        Gesture gesture;

        Log.e("TOUCHES", "" + (touches.size() - 1));
        Log.e("HITS", "" + (hits.size()-1));

        if(touches.isEmpty()) {
            return null;
        }


        Touch t = touches.remove(touches.size() - 1);
        Hit h = hits.remove(hits.size()-1);

        long touchTs = t.getTimestamp();
        long hitTs = h.getTimestamp();

        if(hitTs <= touchTs + 200 && hitTs >= touchTs ){

            Log.e("Detected Hit!", "At " + hitTs + " " + touchTs);
            gesture = new Gesture(t, h);
            return gesture;
        }

        return null;

    }

    public boolean isLearning() {
        return learning;
    }


    public void writeTemplates() {
        pd.sendBang("writeTemplates");
    }

    public void startLearning(){
        pd.sendBang("learnOn");
        learning = true;
    }

    public void stopLearning(){
        pd.sendBang("learnOff");
        learning = false;
    }

    public void forgetTemplate(){
        pd.sendBang("forgetTemplate");
    }

    public void readTemplates() {
        pd.sendBang("readTemplates");
    }

    public String getResult(int template, float  velocity) {

        String result;

        switch(template){
            case 0: result = "TAP";
                break;
            case 1: result = "KNOCK"; break;
            case 2: result = "SLAP"; break;
            case 3: result = "PUNCH"; break;
            default: result = "NOT RECOGNIZED";
        }

        result += " " + velocity;

        return result;
    }


}
