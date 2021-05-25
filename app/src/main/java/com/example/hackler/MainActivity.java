package com.example.hackler;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;

public class MainActivity extends AppCompatActivity {

    private LinkedList<Task> tasks;
    LinkedList<String> xx;
    ListView ts;
    static final private int CHOOSE_THIEF = 0;
    static final private int CHANGE_THIEF = 1;
    ListAdapter adapter;
    private String content_file;
    private String settings_file;
    private String ids_file;
    private Boolean color;
    Intent timer;
    private TimerService timerService;
    App app;

    @Override
    protected void onDestroy() {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("restartservice");
        broadcastIntent.setClass(this, Restarter.class);
        this.sendBroadcast(broadcastIntent);
        super.onDestroy();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        content_file = "content.txt";
        settings_file = "settings.txt";
        ids_file = "id.txt";
        color = (Boolean) resetState(settings_file);
        tasks = (LinkedList<Task>) resetState(content_file);

        App.context = this;
        App.notifys = (ArrayList<Integer>) resetState(ids_file);

        ts = (ListView) findViewById(R.id.tasks);
        setAdapter();

        timerService = new TimerService();
        timer = new Intent(this, timerService.getClass());
        if (!isMyServiceRunning(timerService.getClass())) {
            startService(timer);
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                System.out.println("Service status - Running");
                return true;
            }
        }
        System.out.println("Service status - Not running");
        System.out.println("Service status - Not running");
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        if (color) {
            MenuItem item = menu.findItem(R.id.action_colors);
            item.setTitle(R.string.colors_delete);
        }

        return true;
    }


    private void setAdapter(){
        Task[] tss = new Task[tasks.size()];
        tasks.toArray(tss);
        adapter = new ListAdapter(this,this,  tss, color);

        ts.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_clear_all:
                tasks = new LinkedList<>();
                setAdapter();
                saveState(content_file, tasks);
                return true;
            case R.id.action_sort_by_date:
                Collections.sort(tasks);
                System.out.println("x");
                saveState(content_file, tasks);
                setAdapter();
                return true;
            case R.id.action_colors:
                if (!color){
                    color = true;
                    item.setTitle(R.string.colors_delete);
                }
                else {
                    color = false;
                    item.setTitle(R.string.colors_add);
                }
                saveState(settings_file, color);
                setAdapter();
                return true;
            case R.id.action_exit:
                finishAndRemoveTask();
                return true;
            default:
                return true;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(this);
    }

    public void addTask(View view) {
        Intent intent = new Intent();
        intent.setClass(this, TaskActivity.class);
        intent.putExtra("index", -1);

        startActivityForResult(intent, CHOOSE_THIEF);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CHOOSE_THIEF) {
            if (resultCode == RESULT_OK) {
                tasks.add((Task) data.getSerializableExtra(TaskActivity.THIEF));

                saveState(content_file, tasks);
                setAdapter();
            }
            else {
                Toast.makeText(this, "Задача не сохранена", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == CHANGE_THIEF) {
            if (resultCode == RESULT_OK) {

                Task nt = (Task) data.getSerializableExtra(TaskActivity.THIEF);
                Task ot = tasks.get((Integer) data.getSerializableExtra(TaskActivity.TH));

                ot.setName(nt.getName());
                ot.setDecsription(nt.getDecsription());
                ot.setDate(nt.getDate());
                ot.setTime(nt.getTime());
                ot.setPriority(nt.getPriority());

                saveState(content_file, tasks);
                setAdapter();
            }
            else {
                Toast.makeText(this, "Изменения не сохранены", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveState(String filename, Object object) {
        try {
            FileOutputStream fos = openFileOutput(filename, Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(object);
            os.close();
            fos.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private Object resetState(String filename) {
        try {
            FileInputStream fis = openFileInput(filename);
            ObjectInputStream is = new ObjectInputStream(fis);
            Object x = is.readObject();
            is.close();
            fis.close();

            return x;
        }
        catch (Exception e){
            e.printStackTrace();
            if (filename == content_file) {
                return new LinkedList<>();
            }
            if (filename == settings_file) {
                return Boolean.FALSE;
            }
            if (filename == ids_file) {
                return new ArrayList<>();
            }

            return null;
        }
    }

    public void remove(int position) {
        this.tasks.remove(position);
        saveState(content_file, tasks);
        setAdapter();
    }

    public void edit(int position) {
        ArrayList<String> x = (new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.priority))));
        String name = tasks.get(position).getName();
        String description = tasks.get(position).getDecsription();
        String date = tasks.get(position).getDate();
        String time = tasks.get(position).getTime();
        Integer priority = x.indexOf(tasks.get(position).getPriority());

        System.out.println(priority);

        Intent intent = new Intent(this, TaskActivity.class);
        intent.putExtra("name", name);
        intent.putExtra("description", description);
        intent.putExtra("date", date);
        intent.putExtra("time", time);
        intent.putExtra("priority", priority);
        intent.putExtra("index", position);

        startActivityForResult(intent, CHANGE_THIEF);
    }
}

class Task implements Serializable, Comparable<Task>{

    private String name;
    private String decsription;
    private String date;
    private String time;
    private String priority;
    private Double hash;

    public Task(String name, String decsription, String date, String time, String priority) {
        this.name = name;
        this.decsription = decsription;
        this.date = date;
        this.time = time;
        this.priority = priority;
        this.hash = Math.random();
    }

    @Override
    public int compareTo(Task t)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        try {
            Date d1 = new Date(sdf.parse(this.getDate() + " " + this.getTime()).getTime());
            Date d2 = new Date(sdf.parse(t.getDate() + " " + t.getTime()).getTime());

            return d1.compareTo(d2);
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public Task() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDecsription() {
        return decsription;
    }

    public void setDecsription(String decsription) {
        this.decsription = decsription;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public int h() {
        return hash.hashCode();
    }
}