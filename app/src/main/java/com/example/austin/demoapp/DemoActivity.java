package com.example.austin.demoapp;

import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.view.ViewGroup;
import android.view.LayoutInflater;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class DemoActivity extends AppCompatActivity {

    public int drinksConsumed;
    public boolean userSex;
    public int userWeight;

    // For time calculations
    public Calendar calendar;
    public ArrayList<Date> drinkTimes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
        // Toolbar setup
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Drink increase button
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        TextView drinkCount = (TextView) findViewById(R.id.DrinkCount);
        drinkCount.setText("Drink Count: 0");

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            // Clicking FAB
            public void onClick(View view) {    addDrink(view); }//onClick

        });//fab.setOnClickListener
    }//onCreate

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_VOLUME_UP)){
            addDrink(findViewById(android.R.id.content));
        }//if keyCode
        return true;
    }//onKeyDown


    // Changes variables for calculation and displays text/snackbar
    public void addDrink(View view){

        calendar = Calendar.getInstance(); // Update to now
        drinkTimes.add(calendar.getTime());
        drinksConsumed = drinkTimes.size();

        drinkDisplay();
        BACDisplay();
        timeDisplay();

        // Snackbar display for action
        Snackbar snackbar = Snackbar.make(view, "1 drink consumed", Snackbar.LENGTH_LONG)
                .setAction("UNDO", new View.OnClickListener() {
                    @Override
                    // Snackbar undo click
                    public void onClick(View view) {
                        if (drinksConsumed != 0) {
                            drinksConsumed--;
                            drinkTimes.remove(drinksConsumed);
                        }//if drinksConsumed

                        // Re-display
                        drinkDisplay();
                        BACDisplay();
                        timeDisplay();

                    }//onClick
                });

        snackbar.setActionTextColor(Color.RED);
        snackbar.show();

    }//addDrink

    // Opens on menu option selection
    public static class settingsDialog extends DialogFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setStyle(DialogFragment.STYLE_NORMAL, R.style.settings_dialog);
        }//onCreate

        @Override
        public void onStart() {
            super.onStart();
            Dialog d = getDialog();
            if (d!=null){
                int width = ViewGroup.LayoutParams.MATCH_PARENT;
                int height = ViewGroup.LayoutParams.MATCH_PARENT;
                d.getWindow().setLayout(width, height);
            }//if
        }//onStart


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            return inflater.inflate(R.layout.settings, container, false);

        }//onCreateView

    }//settingsDialog

    // Event handler for submit button on settingsDialog
    public void submit(View view) {
        EditText weight_input = (EditText) view.getRootView().findViewById(R.id.weight);
        userWeight = Integer.parseInt(weight_input.getText().toString());
    }//submit


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_demo, menu);
        return true;
    }//onCreateOptionsMenu

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id)
        {
            case R.id.action_settings: // Settings option - setup of sex,weight
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                settingsDialog sD_fragment = new settingsDialog();
                sD_fragment.show(ft, "txn_tag");
                return true;

            case R.id.update: // Update option - updates BAC (due to time)
                if (drinksConsumed != 0) {
                    BACDisplay();
                    timeDisplay();
                }//if drinksConsumed
                return true;

            case R.id.reset: // Reset option - resets drink counter to 0
                drinkTimes = new ArrayList<>();
                drinksConsumed = 0;
                drinkDisplay();
                BACDisplay();
                return true;
        }//switch

        return super.onOptionsItemSelected(item);

    }//onOptionsItemsSelected


    // Calculation Methods


    // Calculates BAC based on drinks consumed, sex, weight, and time (min) since start
    public double BAC_calc(int drinks, boolean sex, int weight, double time)
    {
        double sexRatio = sex ? 0.73 : 0.66; // sex ? male : female
        return drinks != 0 ? (drinks * 5.14 / weight * sexRatio) - 0.015 / 60 * time : 0; //BAC Formula

    }//BAC_calc

    // Returns true for male selection and false for female selection
    public void sexSelected(View view)
    {
        userSex = view.getId() == R.id.male;
    }//sexSelected

    // Returns difference in minutes
    public int timeDiff(Date date_1, Date date_2)
    {
        long milli_diff = date_2.getTime() - date_1.getTime();
        return (int) milli_diff / 1000 / 60;
    }//timeDiff

    // Calculates time between initial Date and input Date
    public int elapsedTime(Date date)
    {
        return drinksConsumed != 0 ? timeDiff(drinkTimes.get(0),date) : 0;
    }//elapsedTime

    //Calculates time between previous Date and input Date
    public int prevTime(Date date)
    {
        try {
            return timeDiff(drinkTimes.get(drinksConsumed-2),date);
        }//try
        catch (IndexOutOfBoundsException e) {
            return elapsedTime(date);
        }//catch
    }//prevTime


    // Display Methods


    public void drinkDisplay() {

        TextView drinkCount = (TextView) findViewById(R.id.DrinkCount);
        String drinkText = "Drink Count: " + String.valueOf(drinksConsumed);
        drinkCount.setText(drinkText);

    }//drinkDisplay

    public void BACDisplay() {

        TextView BAC = (TextView) findViewById(R.id.BAC);
        if (userWeight != 0) {
            String BAC_text = "BAC: " + String.format("%1.2g%n", BAC_calc(drinksConsumed,
                    userSex, userWeight, elapsedTime(calendar.getTime())));
            BAC.setText(BAC_text);
        }//if userWeight
        else {  BAC.setText("Input settings for BAC calculation");  }

    }//BACDisplay

    public void timeDisplay() {

        TextView timeFromStart = (TextView) findViewById(R.id.time_start);
        TextView timeFromLast = (TextView) findViewById(R.id.time_last);

        String time_start = "Time since starting: " + String.valueOf(
                elapsedTime(calendar.getTime())) + " mins";
        timeFromStart.setText(time_start);


        String time_last = "Time since last drink: " + String.valueOf(prevTime(
                calendar.getTime())) + " mins";
        timeFromLast.setText(time_last);

    }//timeDisplay

}//DemoActivity
