package com.app.pedro.puredataplugin;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.IBinder;
import android.util.Log;

import org.puredata.android.io.AudioParameters;
import org.puredata.android.service.PdService;
import org.puredata.android.utils.PdUiDispatcher;
import org.puredata.core.PdBase;
import org.puredata.core.utils.IoUtils;
import org.puredata.core.utils.PdDispatcher;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Pedro on 06-03-2015.
 */
public class PureData {

    private InputStream stream;
    private Context appContext;
    private int handlePatch;
    private PdService pdService;
    private File saveFile;
    private PdDispatcher myDispatcher;
    private ServiceConnection pdConnection;


    public PureData(Context c) {

        try {

            myDispatcher = new PdUiDispatcher();
            handlePatch = 0;
            pdService = null;
            stream = c.getResources().getAssets().open("test.zip");
            appContext = c;
            saveFile = c.getFilesDir();

            startService();

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

    public void initPd() {

        PdBase.setReceiver(myDispatcher);
        startAudio();

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

    public void sendFloat(String receiver, String number) {
        float value = Float.parseFloat(number);
        PdBase.sendFloat(receiver, value);
    }

    public void sendBang(String receiver){

        Log.e("BANG", receiver);
        PdBase.sendBang(receiver);
    }

    public float[] getArray(String bang, String sourceArray){

        float[] data = new float[512];
        //PdBase.sendBang(bang);
        PdBase.readArray(data, 0, sourceArray, 0, 512);

        return data;
    }

    public PdDispatcher getMyDispatcher(){
        return myDispatcher;
    }


}
