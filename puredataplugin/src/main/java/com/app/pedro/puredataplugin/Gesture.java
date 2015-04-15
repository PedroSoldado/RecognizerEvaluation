package com.app.pedro.puredataplugin;

import android.util.Pair;

/**
 * Created by Pedro on 06-04-2015.
 */
public class Gesture {

    private Touch touch;
    private Hit hit;

    private int template;
    private float intensity;
    private float colorTemperature;

    protected Gesture(int x, int y, float instrument, float vel, float colorTemp) {

        touch = new Touch(x, y);
        hit = new Hit(instrument,vel,colorTemp);

    }

    protected Gesture(Touch t, Hit h) {

        touch = t;
        hit = h;

    }
}
