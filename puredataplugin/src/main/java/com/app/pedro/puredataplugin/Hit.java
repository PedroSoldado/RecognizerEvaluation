package com.app.pedro.puredataplugin;


/**
 * Created by Pedro on 06-04-2015.
 */
public class Hit {

    private int template;
    private float intensity;
    private float colorTemperature;
    private long timestamp;

    protected Hit(float instrument, float vel, float colorTemp) {

        template = (int) instrument;
        intensity = vel;
        colorTemperature = colorTemp;

        timestamp = System.currentTimeMillis();
        //timestamp = time;
    }


    public int getTemplate() {
        return template;
    }

    public float getIntensity() {
        return intensity;
    }

    public float getColorTemperature() {
        return colorTemperature;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
