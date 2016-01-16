package com.example.austin.demoapp;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;
import android.widget.TextView;

public class DemoActivity extends AppCompatActivity {

    public int drinksConsumed = 0;

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

                drinkCount.setText("Drink Count: " + String.valueOf(drinksConsumed));
                BAC.setText("BAC: " + "");
            }

        });
    }

    public double BAC_calc(int drinks, String sex, int weight, double time)
    {
        double sexRatio = sex.equals("MALE") ? 0.73 : 0.66;
        double BAC = (drinks * 5.14 / weight * sexRatio) - 0.015 * time;

        return BAC;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_demo, menu);
        return true;
    }

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
                Intent intent = new Intent(this,Settings.class);
                this.startActivity(intent);
                return true;
            case R.id.reset:
                drinksConsumed = 0;
                TextView drinkCount = (TextView) findViewById(R.id.DrinkCount);
                drinkCount.setText("Drink Count: 0");
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
