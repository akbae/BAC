package com.example.austin.demoapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
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

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.PointsGraphSeries;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class DemoActivity extends AppCompatActivity {

    private static final String FILE_NAME = "settings";
    private static final String SEX_KEY = "settings_sex";
    private static final String WEIGHT_KEY = "settings_weight";

    private int drinksConsumed = 0;
    private Boolean userSex;
    private Integer userWeight;
    private double BAC = 0d;

    // For time calculations
    private ArrayList<DateTime> drinkTimes = new ArrayList<>();

    // For graph
    private PointsGraphSeries<DataPoint> greenSeries;
    private PointsGraphSeries<DataPoint> orangeSeries;
    private PointsGraphSeries<DataPoint> redSeries;
    private PointsGraphSeries<DataPoint> currentSeries;

    private final int BLUE = Color.rgb(0,150,255);
    private final int GREEN = Color.rgb(0,200,0);
    private final int ORANGE = Color.rgb(240,110,0);
    private final int RED = Color.rgb(220,0,0);

    private ArrayList<DataPoint> datapoints = new ArrayList<>();
    private ArrayList[] positions = { new ArrayList(), new ArrayList(), new ArrayList() };
    private PointsGraphSeries<DataPoint> current;
    private double BAC_max = 0d;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
        // Toolbar setup
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Drink increase button
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            // Clicking FAB
            public void onClick(View view) {
                addDrink(view);
            }//onClick

        });

        // Content initialization
        GraphView graph = (GraphView) findViewById(R.id.graph);
        graph.setVisibility(View.INVISIBLE);

        // Timed updates
        final Handler handler = new Handler();
        Timer timer = new Timer();
        update(handler, timer);

        load(getApplicationContext());

    }//onCreate


    @Override
    // Volume up button adds a drink
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_VOLUME_UP)){
            addDrink(findViewById(android.R.id.content));
        }//if keyCode
        return true;
    }//onKeyDown

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

            case R.id.reset: // Reset option - restarts activity
                stopService(new Intent(this, ContinueService.class));
                finish();
                startActivity(getIntent());
                return true;

            case R.id.exit:
                stopService(new Intent(this, ContinueService.class));
                finish();
                System.exit(0);

        }//switch

        return super.onOptionsItemSelected(item);

    }//onOptionsItemsSelected


    // Changes variables for calculation and displays text/snackbar
    public void addDrink(View view){

        drinkTimes.add(new DateTime());
        drinksConsumed = drinkTimes.size();

        drinkDisplay();
        BACDisplay();
        changeColor();
        timeDisplay(false);

        if(drinksConsumed == 1) {
            graphSetup();
            startService(new Intent(this, ContinueService.class));
        }//if
        else {  graphDisplay(false); }

        // Snackbar display for action
        Snackbar snackbar = Snackbar.make(view, R.string.snackbar, Snackbar.LENGTH_LONG)
                .setAction(R.string.undo, new View.OnClickListener() {
                    @Override
                    // Snackbar undo click
                    public void onClick(View view) {
                        drinksConsumed--;
                        drinkTimes.remove(drinksConsumed);

                        if(drinksConsumed == 0) {
                            GraphView graph = (GraphView) findViewById(R.id.graph);
                            graph.setVisibility(View.INVISIBLE);
                            graph.removeSeries(current);
                            current = new PointsGraphSeries<>();
                        }

                        // Re-display
                        drinkDisplay();
                        BACDisplay();
                        graphDisplay(true);
                        changeColor();
                        timeDisplay(true);

                    }//onClick
                });

        snackbar.setActionTextColor(Color.WHITE);
        ViewGroup group = (ViewGroup) snackbar.getView();
        group.setBackgroundColor(BAC_color());
        snackbar.show();

    }//addDrink

    public void update(final Handler h, Timer t) {

        TimerTask updateTime = new TimerTask() {
            @Override
            public void run() {
                h.post(new Runnable() {
                    public void run() {
                        if (drinksConsumed != 0) {
                            GraphView graph = (GraphView) findViewById(R.id.graph);
                            double now = new DateTime().getSecondOfDay() + new DateTime().getDayOfYear() * 86400;

                            currentDisplay(graph, now);
                            timeDisplay(true);
                        }//if drinksConsumed
                    }//run
                });//post
            }//run
        };//TimerTask

        TimerTask updateBAC = new TimerTask() {
            @Override
            public void run() {
                h.post(new Runnable() {
                    public void run() {
                        if (drinksConsumed != 0) {
                            BACDisplay();
                            changeColor();
                        }//if drinksConsumed
                    }//run
                });//post
            }//run
        };//TimerTask

        t.schedule(updateTime, 0, 60000);
        t.schedule(updateBAC, 0, 240000);

    }//update


    // Calculation Methods


    // Calculates BAC based on drinks consumed, sex, weight, and time (min) since start
    public double BAC_calc(int drinks, boolean sex, int weight, double time)
    {
        double sexRatio = sex ? 0.58 : 0.49; // sex ? male : female
        double calc = (drinks * 0.967 / (weight * 0.454 * sexRatio)) - 0.017 / 60 * time;
        return drinks != 0 && calc > 0 ? calc : 0.0; //BAC Formula

    }//BAC_calc

    // Returns difference in minutes
    public int timeDiff(DateTime date_1, DateTime date_2)
    {
        long milli_diff = date_2.getMillis() - date_1.getMillis();
        return (int) milli_diff / 1000 / 60;
    }//timeDiff

    // Calculates time between initial Date and input Date
    public int elapsedTime(DateTime date)
    {
        return drinksConsumed != 0 ? timeDiff(drinkTimes.get(0),date) : 0;
    }//elapsedTime

    //Calculates time between previous Date and input Date
    public int prevTime(DateTime date, boolean isUpdate)
    {
        try {
            return timeDiff(drinkTimes.get(drinksConsumed-2 + (isUpdate ? 1 : 0)),date);
        }//try
        catch (IndexOutOfBoundsException e) {
            return elapsedTime(date);
        }//catch
    }//prevTime

    // Converts int minutes to a string formatted as time
    public String minToDisplay(int minutes)
    {
        String hrText = "";
        String minText = "";

        int hours = minutes / 60;
        minutes = minutes % 60;

        if (hours != 0) {
            hrText = String.valueOf(hours) + " hr ";
        }//if hours

        if (hours == 0 || minutes != 0) {
            minText = String.valueOf(minutes) + " min";
        }//if hours

        return hrText + minText;

    }//minToDisplay


    // Display Methods


    public void drinkDisplay() {

        TextView drinkCount = (TextView) findViewById(R.id.DrinkCount);
        String drinkText = "Drink Count: " + String.valueOf(drinksConsumed);
        drinkCount.setText(drinkText);

    }//drinkDisplay

    public void BACDisplay() {

        TextView BAC_view = (TextView) findViewById(R.id.BAC);
        if (userWeight != 0) {
            BAC = BAC_calc(drinksConsumed,userSex, userWeight, elapsedTime(new DateTime()));
            BAC_max = BAC > BAC_max ? BAC : BAC_max;
            String BAC_text = "BAC: " + String.format("%1.2g%n", BAC);
            BAC_view.setText(BAC_text);
        }//if userWeight
        else {  BAC_view.setText(R.string.init_settings);  }

    }//BACDisplay

    public void timeDisplay(boolean isUpdate) {

        TextView timeFromStart = (TextView) findViewById(R.id.time_start);
        TextView timeFromLast = (TextView) findViewById(R.id.time_last);

        String time_start = "Started " + minToDisplay(elapsedTime(new DateTime())) + " ago";
        timeFromStart.setText(time_start);


        String time_last = "Last drank " + minToDisplay(prevTime(new DateTime(), isUpdate)) + " ago";
        timeFromLast.setText(time_last);

    }//timeDisplay

    public void graphDisplay(boolean isUndo) {

        GraphView graph = (GraphView) findViewById(R.id.graph);
        double now = new DateTime().getSecondOfDay() + new DateTime().getDayOfYear() * 86400;

        if(isUndo) {
            datapoints.remove(datapoints.size() - 1);
            int cPosition = colorToPosition(currentSeries.getColor());
            positions[cPosition].remove((positions[cPosition].size() - 1));

            if(positions[cPosition].size() == 0) {
                graph.removeSeries(currentSeries);
                currentSeries = new PointsGraphSeries<>();
            }
            else {
                currentSeries.resetData(colorData(datapoints, positions[cPosition]));
            }
        }
        else {
            int currentColor = BAC_color();
            if (currentColor == RED) {
                currentSeries = redSeries;
                positions[2].add(datapoints.size());
            } else if (currentColor == ORANGE) {
                currentSeries = orangeSeries;
                positions[1].add(datapoints.size());
            } else if (currentColor == GREEN) {
                currentSeries = greenSeries;
                positions[0].add(datapoints.size());
            }

            DataPoint[] data = new DataPoint[1];
            data[0] = new DataPoint(now, BAC);
            try {
                currentSeries.appendData(data[0], false, 100);
            }//try
            catch (NullPointerException e) {
                currentSeries = new PointsGraphSeries<>(data);

                graph.addSeries(currentSeries);
            }//catch
            datapoints.add(data[0]);

            setShape(currentSeries);
            currentSeries.setColor(currentColor);
        }
        currentDisplay(graph, now);
    }//graphDisplay

    public void currentDisplay(GraphView graph, double now) {
        DataPoint[] data = new DataPoint[1];
        data[0] = new DataPoint(now,BAC);

        current.resetData(data);

        graph.getViewport().setMaxX(now + 10);
        graph.getViewport().setMaxY(BAC_max * 1.25);

        int color = BAC_color();

        current.setColor(color);
        graph.getGridLabelRenderer().setVerticalLabelsColor(color);
    }

    public void graphSetup() {
        GraphView graph = (GraphView) findViewById(R.id.graph);
        GridLabelRenderer label = graph.getGridLabelRenderer();
        Viewport view = graph.getViewport();
        label.setLabelFormatter(new DefaultLabelFormatter() {

            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) {

                    int hour = ((((int) value) % 86400) / 3600) % 12;
                    if (hour == 0) {
                        hour = 12;
                    }

                    int min = ((((int) value) % 86400) % 3600) / 60;

                    return String.valueOf(hour) + String.format(":%02d", min);
                }//if isValueX
                else {
                    return String.format("%1.2f%n", value);
                }//else
            }//formatLabel
        });//graph

        double start = (double) drinkTimes.get(0).getSecondOfDay() + drinkTimes.get(0).getDayOfYear()*86400;

        label.setGridColor(Color.WHITE);
        label.setHorizontalLabelsColor(Color.WHITE);
        label.setVerticalLabelsColor(BAC_color());
        view.setXAxisBoundsManual(true);
        view.setYAxisBoundsManual(true);
        view.setMinX(start - 15);
        view.setMaxX(start + 120);
        view.setMinY(0d);
        view.setMaxY(0.05d);
        view.setScalable(true);
        view.setScrollable(true);

        DataPoint[] data = new DataPoint[1];
        data[0] = new DataPoint(start,BAC);
        datapoints.add(data[0]);
        currentSeries = greenSeries;
        try {
            currentSeries.resetData(data);
            current.resetData(data);
        }//try
        catch (NullPointerException e) {
            currentSeries = new PointsGraphSeries<>(data);
            current = new PointsGraphSeries<>(data);

            graph.addSeries(currentSeries);
            graph.addSeries(current);
        }//catch

        graph.setVisibility(View.VISIBLE);

        setShape(currentSeries);
        positions[0].add(0);

        current.setCustomShape(new PointsGraphSeries.CustomShape() {
            @Override
            public void draw(Canvas canvas, Paint paint, float x, float y, DataPointInterface dataPoint) {
                paint.setStrokeWidth(10);
                canvas.drawLine(x - 10, y, x + 10, y, paint);
                canvas.drawLine(x, y - 10, x, y + 10, paint);
            }
        });

        currentSeries.setColor(GREEN);
        current.setColor(GREEN);
        label.reloadStyles();

    }//graphSetup

    public int colorToPosition(int color) {
        if(color == GREEN) {
            return 0;
        }
        else if(color == ORANGE) {
            return 1;
        }
        else {
            return 2;
        }
    }

    public DataPoint[] colorData(ArrayList data, ArrayList pos) {
        DataPoint[] cData = new DataPoint[pos.size()];
        for(int p = 0; p < pos.size(); p++) {
            cData[p] = (DataPoint)data.get((int)pos.get(p));
        }
        System.out.println(cData.length);
        return cData;
    }

    public void setShape(PointsGraphSeries s) {
        s.setCustomShape(new PointsGraphSeries.CustomShape() {
            @Override
            public void draw(Canvas canvas, Paint paint, float x, float y, DataPointInterface dataPoint) {
                paint.setStrokeWidth(10);
                canvas.drawLine(x - 25, y - 30, x - 15, y + 30, paint);
                canvas.drawLine(x - 15, y + 30, x + 15, y + 30, paint);
                canvas.drawLine(x + 15, y + 30, x + 25, y - 30, paint);
                canvas.drawLine(x + 25, y - 30, x - 25, y - 30, paint);
            }
        });
    }


    // Color Methods


    // Changes the colors of the drink/BAC text, floating action button, and toolbar
    public void changeColor() {

        TextView drinkCount = (TextView) findViewById(R.id.DrinkCount);
        TextView BAC_view = (TextView) findViewById(R.id.BAC);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        int color = BAC_color();

        drinkCount.setTextColor(color);
        BAC_view.setTextColor(color);
        fab.setBackgroundTintList(ColorStateList.valueOf(color));
        toolbar.setBackgroundTintList(ColorStateList.valueOf(color));

    }//changeColor

    // Returns different colors based on BAC
    public int BAC_color()
    {
        int change;
        if (BAC > 0.18) {
            change = RED;
        }//red
        else if (BAC > 0.08) {
            change = ORANGE;
        }//orange
        else if (BAC > 0) {
            change = GREEN;
        }//green
        else {
            change = BLUE;
        }//blue

        return change;
    }//BAC_color


    // Load/save methods


    public void load(Context context)
    {
        SharedPreferences settings;
        settings = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        userSex = settings.getBoolean(SEX_KEY, false);
        userWeight = settings.getInt(WEIGHT_KEY, 0);
    }//load

    public void save(Context context, Boolean isMale, int weight)
    {
        SharedPreferences settings;
        SharedPreferences.Editor editor;
        settings = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        editor = settings.edit();
        editor.clear();

        editor.putBoolean(SEX_KEY, isMale);
        editor.putInt(WEIGHT_KEY, weight);
        editor.apply();
    }//save




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
        try {
            userWeight = Integer.parseInt(weight_input.getText().toString());
            save(getApplicationContext(),userSex,userWeight);
        }//try
        catch(NumberFormatException e) { System.out.println("No input"); }// Do nothing
        finish();
        startActivity(getIntent());
    }//submit

    // Event handler for radio buttons on settingsDialog:
    // Returns true for male selection and false for female selection
    public void sexSelected(View view)
    {
        userSex = view.getId() == R.id.male;
    }//sexSelected

}//DemoActivity
