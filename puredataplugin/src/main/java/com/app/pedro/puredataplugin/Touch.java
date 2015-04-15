package com.app.pedro.puredataplugin;

import android.util.Pair;

/**
 * Created by Pedro on 06-04-2015.
 */
public class Touch {

    private Pair<Integer, Integer> location;
    private long timestamp;

    protected Touch(int x, int y) {

        location = new Pair(x,y);
        timestamp = System.currentTimeMillis();

    }

    public Pair<Integer, Integer> getLocation() {
        return location;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
