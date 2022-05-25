package com.leticija.bugy.concurrent;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class TaskQueue {

    private static BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(10);

    public static TaskBundle prepare() {
        return new TaskBundle();
    }


    public static void subscribe(Runnable r) {
        queue.offer(r);
        //return ;
    }

    static {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    Runnable r = null;
                    try {
                        r = queue.take();
                        System.out.println("Uzel sam task s queuea!");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    r.run();
                }
            }
        }).start();
    }
}
