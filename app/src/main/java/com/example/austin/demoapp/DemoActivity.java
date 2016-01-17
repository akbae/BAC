package com.example.austin.demoapp;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.view.ViewGroup;
import android.view.LayoutInflater;

public class DemoActivity extends AppCompatActivity {

    public int drinksConsumed = 0;
    public boolean userSex;
    public int userWeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        TextView drinkCount = (TextView) findViewById(R.id.DrinkCount);

        drinkCount.setText("Drink Count: 0");

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "1 drink consumed", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show(); // listens for drink
                drinksConsumed++;

                TextView drinkCount = (TextView) findViewById(R.id.DrinkCount);
                TextView BAC = (TextView) findViewById(R.id.BAC);

                String drinkText = "Drink Count: " + String.valueOf(drinksConsumed);
                drinkCount.setText(drinkText);
                String BAC_text = "BAC: " + String.format("%1.2g%n", BAC_calc(drinksConsumed,
                                                                        userSex, userWeight, 60));
                BAC.setText(BAC_text);
            }//onClick

        });//fab.setOnClickListener

    }//onCreate

    public static class settingsDialog extends DialogFragment {


        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setStyle(DialogFragment.STYLE_NORMAL, R.style.settings_dialog);
        }

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

    public void submit(View view)
    {

        EditText weight_input = (EditText) view.getRootView().findViewById(R.id.weight);

        setWeight(Integer.parseInt(weight_input.getText().toString()));
    }

    public void setWeight(int wt)
    {
        userWeight = wt;
    }

    public void sexSelected(View view)
    {
        userSex = view.getId() == R.id.male;
    }//sexSelected

    public double BAC_calc(int drinks, boolean sex, int weight, double time)
    {
        double sexRatio = sex ? 0.73 : 0.66; // sex ? male : female

        return (drinks * 5.14 / weight * sexRatio) - 0.015 /60 * time; //BAC Formula
    }//BAC_calc

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
            case R.id.action_settings:
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                settingsDialog sD_fragment = new settingsDialog();
                sD_fragment.show(ft, "txn_tag");
                return true;

            case R.id.update:
                if (drinksConsumed != 0) {
                    TextView BAC = (TextView) findViewById(R.id.BAC);
                    String BAC_text = "BAC: " + "";
                    BAC.setText(BAC_text);
                }
                return true;

            case R.id.reset:
                drinksConsumed = 0;
                TextView drinkCount = (TextView) findViewById(R.id.DrinkCount);
                drinkCount.setText("Drink Count: 0");
                TextView BAC = (TextView) findViewById(R.id.BAC);
                BAC.setText("");
                return true;
        }//switch

        return super.onOptionsItemSelected(item);
    }//onOptionsItemsSelected

}//DemoActivity
