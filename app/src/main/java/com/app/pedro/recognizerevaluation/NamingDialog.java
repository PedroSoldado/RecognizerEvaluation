package com.app.pedro.recognizerevaluation;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

/**
 * Class that creates the naming dialog
 */
public class NamingDialog extends DialogFragment {

    LayoutInflater inflater;
    AlertDialog.Builder builder;
    TableLayout table;


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        builder = new AlertDialog.Builder(getActivity());
        inflater = getActivity().getLayoutInflater();

        View view = inflater.inflate(R.layout.naming_dialog, null);

        table = (TableLayout) view.findViewById(R.id.namingTable);

        builder.setView(view);

        int templates = MainActivity.recognizer.getTemplatesNumber();
        Log.e("NUMBEER", templates + "");

        createNamingTable(templates, view);

        // Add action buttons
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

                if(saveNames()) {
                    nListener.onDialogPositiveClick(NamingDialog.this);
                    Log.e("Confirm", "YES");
                    return;
                }

                Log.e("Please fill", "PLEASE");

                Context context = getActivity().getApplicationContext();
                CharSequence text = "Please name all the gestures.";
                int duration = Toast.LENGTH_LONG;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();

            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Log.e("Cancel", "YES");
                nListener.onDialogNegativeClick(NamingDialog.this);
                NamingDialog.this.getDialog().cancel();
            }
        });


        return builder.create();
    }

    private boolean saveNames() {

        HashMap<Integer, String> nameMap = new HashMap<>();
        int template;
        String name;

        for (int i = 0; i < table.getChildCount(); i++) {
            TableRow tr = (TableRow) table.getChildAt(i);

            EditText className = (EditText) tr.getChildAt(1);
            name = className.getText().toString().toUpperCase();

            if(name.isEmpty())
                return false;

            TextView classNumber = (TextView) tr.getChildAt(0);
            template = Integer.parseInt(classNumber.getText().toString());
            nameMap.put(template,name);
        }
        writeNamesFile(nameMap);
        MainActivity.recognizer.loadNames(nameMap);

        return true;
    }

    private void writeNamesFile(HashMap<Integer, String> nameMap) {

        File root = android.os.Environment.getExternalStorageDirectory();
        File dir = new File(root.getAbsolutePath() + getString(R.string.folder));
        dir.mkdirs();
        File file = new File(dir, getString(R.string.namesFile));

        try {
            FileOutputStream f = new FileOutputStream(file, false);
            PrintWriter pw = new PrintWriter(f);

            for(int number : nameMap.keySet()) {
                pw.append(number + "\t" + nameMap.get(number) + "\n");
            }

            pw.flush();
            pw.close();
            f.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     *
     * Closes the soft keyboard after the dialog is closed (both on positive or negative cases)
     * @param d
     */
    @Override
    public void onDismiss(DialogInterface d){
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

    }


    private void createNamingTable(int rows, View view) {

        TableLayout t = (TableLayout) view.findViewById(R.id.namingTable);


        for (int i = 0; i < rows; i++) {

            TableRow tr = (TableRow) inflater.inflate(R.layout.dialog_tablerow, null);

            TextView classNumber = (TextView) tr.getChildAt(0);
            classNumber.setText(i + "");
            t.addView(tr);

        }

    }


    public interface NamingDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog);
        public void onDialogNegativeClick(DialogFragment dialog);
    }

    // Use this instance of the interface to deliver action events
    NamingDialogListener nListener;

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            nListener = (NamingDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString() + " must implement NamingDialogListener");
        }
    }

}
