package com.diandi.klob.player.spider.concurrent;

import android.os.Handler;
import android.os.Looper;

/**
 * *******************************************************************************
 * *********    Author : klob(kloblic@gmail.com) .
 * *********    Date : 2015-11-21  .
 * *********    Time : 22:48 .
 * *********    Version : 1.0
 * *********    Copyright © 2015, klob, All Rights Reserved
 * *******************************************************************************
 */
public class RequestProcessor {
    static Handler interHandler;

    static {
        interHandler = new Handler(Looper.getMainLooper());
    }

    public  static void execute(final WorkHandler handler) {
        if (handler != null) {
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    handler.start();
                    interHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            handler.over();
                        }
                    });
                }
            }.start();
        }
    }
}
