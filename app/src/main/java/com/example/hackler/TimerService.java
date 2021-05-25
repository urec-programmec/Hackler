package com.example.hackler;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

@RequiresApi(api = Build.VERSION_CODES.O)
public class TimerService extends Service {

    private final Timer timer = new Timer();
    private String filename = "content.txt";
    private String ids = "id.txt";
    private String CHANNEL_ID = "HacklerID";
    private String CHANNEL_NAME = "Hackler";


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
            startMyOwnForeground();
        else
            startForeground(1, new Notification());
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void startMyOwnForeground()
    {
        String NOTIFICATION_CHANNEL_ID = "example.permanence";
        String channelName = "Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle("App is running in background")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int interval = 60000;
        timer.scheduleAtFixedRate(new TimerTask(){
            @Override
            public void run(){
                Object content = getContent(filename);
                if (content != null){
                    LinkedList<Task> tasks = (LinkedList<Task>) content;

                    ArrayList<Integer> x = new ArrayList<>();
                    for (int i = 0; i < tasks.size(); i++){
                        x.add(tasks.get(i).h());
                    }

                    for (int i = 0; i < App.notifys.size(); i++){
                        if (!x.contains(App.notifys.get(i)))
                            App.notifys.remove(i);
                    }
                    setContent(ids, App.notifys);

                    for (int i = 0; i < tasks.size(); i++){
                        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
                        Date dt = null;
                        try {
                            dt = new Date(sdf.parse(tasks.get(i).getDate() + " " + tasks.get(i).getTime()).getTime());
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        Date now = new Date(System.currentTimeMillis());
                        if (dt.equals(now) || dt.before(now)){
                            Task task = tasks.get(i);

                            int id = task.h();

                            if (!App.notifys.contains(id)) {

                                App.notifys.add(id);
                                setContent(ids, App.notifys);

                                Intent notificationIntent = new Intent(App.context, MainActivity.class);
                                PendingIntent contentIntent = PendingIntent.getActivity(App.context,
                                        0, notificationIntent,
                                        PendingIntent.FLAG_CANCEL_CURRENT);

                                NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                Notification.Builder builder = new Notification.Builder(App.context);
                                builder.setContentInfo("content info")
                                        .setContentTitle(task.getName() + "\n(" + task.getDate() + " " + task.getTime() + ")")
                                        .setContentText(task.getDecsription())
                                        .setSmallIcon(R.drawable.icon)
                                        .setContentIntent(contentIntent);

                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                    NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "my_channel", NotificationManager.IMPORTANCE_DEFAULT);
                                    manager.createNotificationChannel(channel);
                                    builder.setChannelId(CHANNEL_ID);
                                }

                                manager.notify(id, builder.build());

                            }
                        }
                    }
                }
                else
                    System.out.println("ОШИБКА");
            }
        },0, interval);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        timer.cancel();
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("restartservice");
        broadcastIntent.setClass(this, Restarter.class);
        this.sendBroadcast(broadcastIntent);
    }

    private Object getContent(String filename) {
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
            return null;
        }
    }

    private void setContent(String filename, Object object) {
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
}
