package com.app.pedro.puredataplugin;


/**
 * Created by Pedro on 06-04-2015.
 */
public class Hit {

    private int template;
    private float velocity;
    private float colorTemperature;

    private float intensity;

    private long timestamp;

    protected Hit(float instrument, float vel, float colorTemp, float intens) {

        template = (int) instrument;
        velocity = vel;
        colorTemperature = colorTemp;

        intensity = intens;

        timestamp = System.currentTimeMillis();
        //timestamp = time;
    }


    public int getTemplate() {
        return template;
    }

    public float getVelocity() {
        return velocity;
    }

    public float getColorTemperature() {
        return colorTemperature;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
