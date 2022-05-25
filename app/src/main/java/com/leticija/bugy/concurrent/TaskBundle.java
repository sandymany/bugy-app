package com.leticija.bugy.concurrent;

import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.List;

public class TaskBundle {

    private List<Runnable> runnables = new ArrayList<>();

    public TaskBundle backgroundTask(Runnable r) {
        runnables.add(r);
        return this;
    }

    public TaskBundle guiTask(final Runnable r) {
        Runnable R = new Runnable() {
            @Override
            public void run() {
                android.os.Handler handler = new Handler(Looper.getMainLooper());
                handler.post(r);
            }
        };
        runnables.add(R);
        return this;
    }

    public void subscribeMe() {
        for(Runnable r : runnables) {
            TaskQueue.subscribe(r);
        }
    }
}