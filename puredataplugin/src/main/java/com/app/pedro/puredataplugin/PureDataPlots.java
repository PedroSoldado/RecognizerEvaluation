package com.app.pedro.puredataplugin;

import android.content.Context;

/**
 * Created by Pedro on 08-06-2015.
 */
public class PureDataPlots {

    private PureData pd;
    private Context appContext;

    public PureDataPlots(Context context) {

        pd = new PureData(context);

    }

    public PureData getPdInstance(){
        return pd;
    }

    public float[] receiveMagnitude(){

        return pd.getArray("getMagnitude", "magnitude");

    }

    public float[] receiveEnvelope(){

        return pd.getArray("getMagnitude", "envelope");

    }
}
