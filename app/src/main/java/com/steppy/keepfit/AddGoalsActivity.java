package com.steppy.keepfit;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class AddGoalsActivity extends AppCompatActivity {
    MainActivity mainact;
    private BottomBar bottomBar;
    final Handler handler = new Handler();
    private ArrayList<Goal> listToBeSaved = new ArrayList<>();

    private int mYear, mMonth, mDay = 0;

    private DBHelper dbHelper;
    private boolean testMode;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_goals);

        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(this);
        testMode = SP.getBoolean("enableTest", false);
        //Toast.makeText(this, Boolean.toString(testMode), Toast.LENGTH_LONG).show();

        Spinner unitsSpin = (Spinner) findViewById(R.id.spinnerUnits);

        if(testMode){
            EditText steps = (EditText) findViewById(R.id.stepsText); //new EditText(this);
            steps.setVisibility(View.VISIBLE);
            steps.setHint("Steps Taken");

            final Button dateBut = (Button) findViewById(R.id.addDateBut);//new Button(this);
            dateBut.setVisibility(View.VISIBLE);
            dateBut.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    final Calendar c = Calendar.getInstance();
                    mYear = c.get(Calendar.YEAR);
                    mMonth = c.get(Calendar.MONTH);
                    mDay = c.get(Calendar.DAY_OF_MONTH);

                    DatePickerDialog dpd = new DatePickerDialog(AddGoalsActivity.this,
                            new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePicker view, int year,
                                                      int monthOfYear, int dayOfMonth) {
                                    mYear=year;
                                    mMonth=monthOfYear;
                                    mDay = dayOfMonth;
                                    dateBut.setText(mDay+"/"+(mMonth+1+"/"+mYear));
                                }
                            }, mYear, mMonth, mDay);
                    dpd.show();
                }
            });
        }

        Button but = (Button) findViewById(R.id.setButton);
        but.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(testMode){
                    dbHelper = new DBHelper(AddGoalsActivity.this);
                    EditText name = (EditText) findViewById(R.id.nameText);
                    EditText goalValue = (EditText) findViewById(R.id.goalsText);
                    EditText stepsValue = (EditText) findViewById(R.id.stepsText);
                    String nameString = name.getText().toString();
                    String goalString = goalValue.getText().toString();
                    String stepsString = stepsValue.getText().toString();
                    Date date = new GregorianCalendar(mYear,mMonth,mDay).getTime();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    String dateString1 = sdf.format(date);
                    Spinner unitsSpin = (Spinner) findViewById(R.id.spinnerUnits);
                    String units = unitsSpin.getSelectedItem().toString();
                    //Toast.makeText(AddGoalsActivity.this,"OLDGOAL:"+dateString1,Toast.LENGTH_SHORT).show();

                    float i =Float.parseFloat(goalString);
                    float goalValueInt = turnIntoSteps(i);
                    float stepsProgressInt = turnIntoSteps(Float.parseFloat(stepsString));
                    //int goalValueInt = Integer.parseInt(goalString);
                    //int stepsProgressInt = Integer.parseInt(stepsString);

                    float percent = ((float)stepsProgressInt/(float)goalValueInt) *100;
                    int percentInt = (int) percent;
                    String percentString = Integer.toString(percentInt);

                    Toast.makeText(AddGoalsActivity.this,"OLDGOAL:"+goalValueInt+" "+stepsProgressInt,Toast.LENGTH_SHORT).show();

                    if(dbHelper.insertOldGoal(nameString,(int)goalValueInt,(int)stepsProgressInt,percentString,dateString1,units)){
                        Toast.makeText(AddGoalsActivity.this, "Test Goal Added Successfully",Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(AddGoalsActivity.this, "Failed to add", Toast.LENGTH_LONG).show();
                    }
                    dbHelper.close();

                }else {

                    EditText name = (EditText) findViewById(R.id.nameText);
                    EditText goal = (EditText) findViewById(R.id.goalsText);
                    Spinner unitsSpin = (Spinner) findViewById(R.id.spinnerUnits);
                    String units = unitsSpin.getSelectedItem().toString();

//                    Calendar cal = Calendar.getInstance();
//                    cal.setTime(date);
//                    int day = cal.get(Calendar.DAY_OF_MONTH);
//                    int month = cal.get(Calendar.MONTH);
//                    int year = cal.get(Calendar.YEAR);

                    dbHelper = new DBHelper(AddGoalsActivity.this);
                    String nameString = name.getText().toString();
                    String goalString = goal.getText().toString();


                    float stepsGoal = turnIntoSteps(Float.parseFloat(goalString));

                    Date date = new Date();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    String dateString = sdf.format(date);

                    Toast.makeText(AddGoalsActivity.this,""+stepsGoal,Toast.LENGTH_SHORT).show();

                    if (dbHelper.insertGoal(nameString,(int)stepsGoal, "false", dateString,units)) {
                        Toast.makeText(AddGoalsActivity.this, "Goal Added Successfully", Toast.LENGTH_LONG).show();
                    }else{
                        Toast.makeText(AddGoalsActivity.this, "Failed to add", Toast.LENGTH_LONG).show();
                    }
                    dbHelper.close();
                }
                backToPrevFrag();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void backToPrevFrag(){
        Intent fragBack = new Intent(this, MainActivity.class);
        fragBack.putExtra("openPrevFrag",true);
        startActivity(fragBack);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public float turnIntoSteps(float goalInSomeUnits){

        Spinner unitsSpin = (Spinner) findViewById(R.id.spinnerUnits);
        String units = unitsSpin.getSelectedItem().toString();
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(AddGoalsActivity.this);

        float stepsInt = goalInSomeUnits;
        float goalFloat = (float) goalInSomeUnits;
        float steps=0;
        float inches=0;
        float cms=0;
        float inch=0;
        float cm=0;
        switch (units){
            case "Metres":
//              // 1 meters = 100cm
                cms = goalFloat*100;
                cm = SP.getFloat("mappingMet",75);
                steps = cms/cm;
                stepsInt = (int) steps;
                break;
            case "Kilometres":
                // 1 kilometer = 1000 metres
                float m = goalFloat*1000;
                // 1 meters = 100cm
                cms = m*100;
                cm = Float.parseFloat(SP.getString("mappingMet","75"));
                steps = cms/cm;
                stepsInt = (int) steps;
                break;
            case "Yards":
                //1 yard = 36 inches
                inches = goalFloat*36;
                inch = (float) SP.getInt("mappingImp",30);
                steps = inches/inch;
                stepsInt = (int) steps;
                break;
            case "Miles":
                //1 mile = 1760 yards
                float yards = goalFloat*1760;
                //1 yard = 36 inches
                inches = yards*36;
                inch = Integer.parseInt(SP.getString("mappingImp","30"));
                steps = inches/inch;
                // steps in inches atm
                stepsInt = (int) steps;
                break;
        }

        return stepsInt;
    }
}
