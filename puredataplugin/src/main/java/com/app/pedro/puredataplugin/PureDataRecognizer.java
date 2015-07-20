package com.app.pedro.puredataplugin;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Pedro on 23-04-2015.
 */
public class PureDataRecognizer {

    private PureData pd;
    private Context appContext;

    private List<Hit> hits;
    private List<Touch> touches;
    private HashMap<Integer, String> nameMap;
    //Map the template to a list of 3 floats - 3 levels of intensity (weak, medium, strong)
    private HashMap<Integer, List<Float>> intensityMap;
    private int numberTemplatesLearned;

    public PureDataRecognizer(Context context) {

        pd = new PureData(context);
        appContext = context;

        numberTemplatesLearned = 0;
        hits = new ArrayList<>();
        touches = new ArrayList<>();
        intensityMap = new HashMap<>();
        nameMap = new HashMap<>();
    }

    /**
     * Adds the touch when detected on the screen to be analyzed.
     * Must be attached to the activity's touch listener.
     *
     * @param x The touch's x coordinate on the screen
     * @param y The touch's y coordinate on the screen
     */
    public void addTouch(int x, int y) {

        Touch touch = new Touch(x,y);
        touches.add(touch);
    }

    /**
     *
     *
     * @param template
     * @param velocity
     * @param color
     * @param intensity
     */
    public void addHit(float template, float velocity, float color, float intensity) {

        Hit hit = new Hit(template,velocity,color, intensity);

        if(hits.size() > 9)
            hits.clear();

        hits.add(hit);
    }

    /**
     * Detect a hit on the screen, by comparing the touch with recorder hit timestamps
     *
     * @return Returns the detected Gesture (Hit + Touch information)
     */
    public Gesture detectHit() {

        Gesture gesture;

        if(touches.isEmpty()) {
            return null;
        }

        Touch t = touches.remove(touches.size() - 1);
        Hit h = hits.remove(hits.size()-1);

        long touchTs = t.getTimestamp();
        long hitTs = h.getTimestamp();

        if(hitTs <= touchTs + 200 && hitTs >= touchTs ){

            //Log.e("Detected Hit!", "At " + hitTs + " " + touchTs);
            gesture = new Gesture(t, h);
            return gesture;
        }

        return null;

    }

    public boolean importFiles(File nameFile, File templateFile, File intensitiesFile) {

        int linesNameFile = countLines(nameFile);
        int linesTemplateFile = countLines(templateFile);
        int linesIntensitiesFile = countLines(intensitiesFile);

        if (!(linesNameFile == linesTemplateFile && linesTemplateFile == linesIntensitiesFile)) {

            return false;
        }

        try {

            BufferedReader br = new BufferedReader(new FileReader(intensitiesFile));
            String line;
            String[] result;
            List<Float> resultFloats = new ArrayList<>();
            int template;

            while ((line = br.readLine()) != null) {
                result = line.split(" ");
                template = Integer.parseInt(result[0]);

                for (int i = 1; i < result.length; i++) {
                    float res = Float.parseFloat(result[i]);
                    resultFloats.add(res);
                }
                loadIntensities(template, resultFloats);
            }

            br = new BufferedReader(new FileReader(nameFile));
            while ((line = br.readLine()) != null) {
                result = line.split("\t");
                template = Integer.parseInt(result[0]);
                String name = result[1];

                nameMap.put(template, name);
            }

            numberTemplatesLearned = nameMap.size();

            File dir = appContext.getFilesDir();
            File file = new File(dir, "templates.txt");

            InputStream is=new FileInputStream(templateFile);
            OutputStream os=new FileOutputStream(file);
            byte[] buff=new byte[1024];
            int len;
            while((len=is.read(buff))>0){
                Log.e("WRITING", "YES" );
                os.write(buff, 0, len);
            }
            is.close();
            os.close();

            setTemplatesNumber(linesTemplateFile);
            readTemplates();



        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    private int countLines(File file){
        String line;
        int lines = 0;

        BufferedReader reader;
        try {

            reader = new BufferedReader(new FileReader(file));
            while ((line = reader.readLine()) != null) {
                lines++;
            }
            reader.close();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
        return lines;

    }

    public void writeTemplates() {
        pd.sendBang("writeTemplates");
    }

    public void startLearning(){
        pd.sendBang("learnOn");
    }

    public void stopLearning(){
        pd.sendBang("learnOff");
    }

    public void forgetTemplate(){
        pd.sendBang("forgetTemplate");
    }

    public void readTemplates() {
        pd.sendBang("readTemplates");
    }

    public float[] receiveWaveData(){

        return pd.getArray("getMagnitude", "magnitude");

    }

    public String getResult(int template, float  velocity) {

        String name = nameMap.get(template);
        if(name == null)
            name = "NOT ASSIGNED";

        String result =  name + " - " + compareIntensity(template, velocity);

        return result;
    }

    private HashMap getIntensityMap(){
        return intensityMap;
    }

    private List<Float> getIntensities(int template){
        return intensityMap.get(template);
    }

    private String compareIntensity(int template, float value){

        List<Float> intensitiesValues = intensityMap.get(template);

        if(intensitiesValues == null)
            return "NA";

        float weakValue = intensitiesValues.get(0);
        float mediumValue = intensitiesValues.get(1);
        float strongValue = intensitiesValues.get(2);

        float distanceWeak = Math.abs(weakValue - value);
        float distanceMedium = Math.abs(mediumValue - value);
        float distanceStrong = Math.abs(strongValue - value);

        float smaller = Math.min(distanceWeak, Math.min(distanceMedium,distanceStrong));

        if(smaller == distanceWeak){
            return "Weak";
        }
        else if(smaller == distanceMedium){
            return "Medium";
        }
        else return "Strong";



    }

    public void loadIntensities(int template, List<Float> values){

        intensityMap.put(template, values);
    }

    public void loadIntensities(int template, String values){

        String[] result = values.split(" ");
        List<Float> resultFloats = new ArrayList<Float>();

        for(int i = 1; i < result.length; i++){

            resultFloats.add(Float.parseFloat(result[i]));


        }
        intensityMap.put(template, resultFloats);
    }

    public HashMap getNames() {
        return nameMap;
    }

    public void loadNames(HashMap map){

        nameMap = map;
    }

    public int getTemplatesNumber() {
        return numberTemplatesLearned;
    }

    public void setTemplatesNumber(int quantity){
        numberTemplatesLearned = quantity;
    }
}
