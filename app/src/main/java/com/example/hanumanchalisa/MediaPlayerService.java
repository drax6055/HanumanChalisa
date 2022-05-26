package com.example.hanumanchalisa;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;


public class MediaPlayerService extends Service {

    private IBinder mbinder = new Mybinder();
    public static final String ACTION_NEXT = "NEXT";
    public static final String ACTION_PREV = "PREVIOUS";
    public static final String ACTION_PLAY = "PLAY";
    ActionPlaying actionPlaying;

    @Override
    public IBinder onBind(Intent intent) {
        return mbinder;
    }

    public class Mybinder extends Binder {
        MediaPlayerService getService() {
            return MediaPlayerService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String actionName = intent.getStringExtra("myActivityName");
        if (actionName != null) {
            switch (actionName) {
                case ACTION_PLAY:
                    if (actionPlaying != null) {
                        actionPlaying.playClicked();
                    }
                    break;
                case ACTION_PREV:
                    if (actionPlaying != null) {
                        actionPlaying.prevClicked();
                    }
                    break;
                case ACTION_NEXT:
                    if (actionPlaying != null) {
                        actionPlaying.nextClicked();
                    }
                    break;
            }
        }
        return START_STICKY;
    }
    public void setCallBack(ActionPlaying actionPlaying)
    {
        this.actionPlaying = actionPlaying;
    }

}