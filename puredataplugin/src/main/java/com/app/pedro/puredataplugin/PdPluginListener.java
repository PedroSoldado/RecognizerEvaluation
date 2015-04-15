package com.app.pedro.puredataplugin;

import android.util.Log;

import org.puredata.core.PdListener;

/**
 * Created by Pedro on 07-03-2015.
 */
public class PdPluginListener {

    public String receiveList(String s, Object... objects) {

        String args = "";

        for(Object arg: objects) {
            args += " " + arg.toString();
        }

        Log.e("receiveList", s + " " + args);

        return args;
    }


}
