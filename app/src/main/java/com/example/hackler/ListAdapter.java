package com.example.hackler;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ListAdapter extends ArrayAdapter<Task> {

    private final Activity context;
    private final MainActivity main;
    private Task[] tasks;
    private Boolean coloring;

    public ListAdapter(Activity context, MainActivity main, Task[] tasks, Boolean coloring) {
        super(context, R.layout.list_item, tasks);
        this.coloring = coloring;
        this.context = context;
        this.tasks = tasks;
        this.main = main;
    }

    public View getView(int position,View view,ViewGroup parent) {

        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.list_item, null,true);

        ((TextView) rowView.findViewById(R.id.xname)).setText(tasks[position].getName());
        ((TextView) rowView.findViewById(R.id.xdate)).setText(tasks[position].getDate() + " " + tasks[position].getTime());
        if (coloring){
            if (tasks[position].getPriority().equals(context.getResources().getStringArray(R.array.priority)[0]))
                ((LinearLayout) rowView.findViewById(R.id.item)).setBackgroundResource(R.drawable.priority_low);
            else if (tasks[position].getPriority().equals(context.getResources().getStringArray(R.array.priority)[1]))
                ((LinearLayout) rowView.findViewById(R.id.item)).setBackgroundResource(R.drawable.priority_normal);
            else
                ((LinearLayout) rowView.findViewById(R.id.item)).setBackgroundResource(R.drawable.priority_hard);
        }
        else
            ((LinearLayout) rowView.findViewById(R.id.item)).setBackgroundResource(R.drawable.priority_common);

        ((LinearLayout) rowView.findViewById(R.id.delete)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                main.remove(position);
            }
        });

        ((LinearLayout) rowView.findViewById(R.id.edit)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                main.edit(position);
            }
        });

        return rowView;
    };
}