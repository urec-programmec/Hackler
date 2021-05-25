package com.example.hackler;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;

import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TaskActivity extends AppCompatActivity {

    DatePickerDialog.OnDateSetListener dd;
    TimePickerDialog.OnTimeSetListener tt;
    Calendar dateAndTime;
    TextView date;
    TextView time;
    TextView name;
    TextView description;
    Spinner priority;
    Integer index;
    public final static String THIEF = "com.example.hackler.THIEF";
    public final static String TH = "com.example.hackler.TH";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        date = (TextView) findViewById(R.id.date);
        time = (TextView) findViewById(R.id.time);
        name = (TextView) findViewById(R.id.name);
        description = (TextView) findViewById(R.id.description);
        priority = (Spinner) findViewById(R.id.priority);

        dateAndTime = Calendar.getInstance();
        dd = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                dateAndTime.set(Calendar.YEAR, year);
                dateAndTime.set(Calendar.MONTH, monthOfYear);
                dateAndTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                setInitialDate();
            }
        };

        tt = new TimePickerDialog.OnTimeSetListener() {

            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                dateAndTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                dateAndTime.set(Calendar.MINUTE, minute);
                setInitialTime();
            }
        };

        String[] pr = getResources().getStringArray(R.array.priority);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, pr);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        Spinner spinner = (Spinner) findViewById(R.id.priority);
        spinner.setAdapter(adapter);

        spinner.setSelection(1);
        // устанавливаем обработчик нажатия
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                // показываем позиция нажатого элемента
//                Toast.makeText(getBaseContext(), "Position = " + position, Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        Bundle arguments = getIntent().getExtras();
        index = Integer.parseInt(arguments.get("index").toString());
        if (index != -1){
            System.out.println(index);
            date.setText(arguments.get("date").toString());
            time.setText(arguments.get("time").toString());
            name.setText(arguments.get("name").toString());
            description.setText(arguments.get("description").toString());
            priority.setSelection(Integer.parseInt(arguments.get("priority").toString()));
        }
    }


    public void save(View view) throws ParseException {
        if (!(name.getText().toString().equals("")
                || date.getText().toString().equals("")
                || time.getText().toString().equals(""))) {

            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
            Date dt = new Date(sdf.parse(date.getText().toString() + " " + time.getText().toString()).getTime());
            Date now = new Date(System.currentTimeMillis());
            if (dt.before(now)){
                Toast.makeText(this, "Дата меньше либо равна текущей", Toast.LENGTH_SHORT).show();
                return;
            }

            Task task = new Task(name.getText().toString(),
                    description.getText().toString(),
                    date.getText().toString(),
                    time.getText().toString(),
                    priority.getSelectedItem().toString());

            Intent answerIntent = new Intent();
            if (index != -1)
                answerIntent.putExtra(TH, index);


            answerIntent.putExtra(THIEF, task);
            setResult(RESULT_OK, answerIntent);

            finish();
        }
        else
            Toast.makeText(this, "Не все поля заполнены", Toast.LENGTH_SHORT).show();
    }

    private void setInitialDate() {

        Date dt = dateAndTime.getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");
        date.setText(df.format(dt));
    }

    private void setInitialTime() {

        Date dt = dateAndTime.getTime();
        SimpleDateFormat df = new SimpleDateFormat("HH:mm", Locale.UK);
        time.setText(df.format(dt));
//        time.setText(DateUtils.formatDateTime(this,
//                dateAndTime.getTimeInMillis(),
//                DateUtils.FORMAT_SHOW_TIME));
    }


    public void setDate(View view) {
        new DatePickerDialog(this, dd,
                dateAndTime.get(Calendar.YEAR),
                dateAndTime.get(Calendar.MONTH),
                dateAndTime.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    public void setTime(View view) {
        new TimePickerDialog(this, tt,
                dateAndTime.get(Calendar.HOUR_OF_DAY),
                dateAndTime.get(Calendar.MINUTE), true)
                .show();
    }
}