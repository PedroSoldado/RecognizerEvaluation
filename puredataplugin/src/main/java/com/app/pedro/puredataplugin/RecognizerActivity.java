package com.app.pedro.puredataplugin;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by Pedro on 04-05-2015.
 */
public class RecognizerActivity extends Activity {

    PureDataRecognizer recognizer;

    @Override
    public void onCreate(Bundle bundle){
        super.onCreate(bundle);

        recognizer = new PureDataRecognizer(getApplicationContext());
    }

}
