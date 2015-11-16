# RecognizerEvaluation
Pure Data Gesture Recognizer

This project provides the training and integration modules for the Impact Toolkit, 
which allies acoustic sensing and gesture classification features.
## How-To:

1. Add the module puredataplugin to your Android application project, and include it as a dependency for your app module.

2. Select the Activity in which you want to instantiate the toolkit's features.

a) Declare the Recognizer and PureData instances as global variables:
    public static PureDataRecognizer recognizer;
    public static PureData pd;
    
b) On the onCreate method, at the end add the following lines:

        recognizer = new PureDataRecognizer(getApplicationContext());
        pd = new PureData(getApplicationContext());
        pd.getMyDispatcher().addListener("bonk-cooked", new PdListener.Adapter() {
            @Override
            public void receiveList(String source, Object... objects) {
                int template = (int) Double.parseDouble(objects[0].toString());
                float velocity = Float.parseFloat(objects[1].toString());
                float colorTemperature = Float.parseFloat(objects[2].toString());
                float loudness = Float.parseFloat(objects[3].toString());
                
                recognizer.addHit(template, velocity, colorTemperature, loudness);
            }
        });
These lines initiate the capturing process and retrieve information of the sound events.

c) Create a folder on the device, and add the 3 files:

          File root = android.os.Environment.getExternalStorageDirectory();
          File dir = new File(root.getAbsolutePath() + "/results");
          File namesFile = new File(dir, "namesMapping.txt");
          File templateFile = new File(dir, "templates.txt");
          File intensityFile = new File(dir, "intensityResults.txt");
          
          recognizer.importFiles(namesFile, templateFile, intensityFile);

d) To register a touch event to be detected, add the following line to the dispatchTouchEvent method (or to another touch event handler):

 		if(event.getAction() == MotionEvent.ACTION_DOWN){
            recognizer.addTouch((int) event.getX(), (int) event.getY());
        }

3. Now the toolkit is already receiving sound events and touch events.
After receiving the touch event, on the touch event handler, it is possible to receive the detected gesture and map it to a feature/method:

		String gesture = recognizer.getGesture();
        
a) Using the given name on the namesMapping.txt file, each gesture can be identified.
Map each feature with the corresponding gesture. This can be done in a variety of ways.
Example:

        switch(gesture){
            case "TAP":  doA();
            case "KNOCK": doB();
            case "NAIL":  doC();
        }


The documentation and javadoc is available at http://web.ist.utl.pt/ist170184/impactToolkit
