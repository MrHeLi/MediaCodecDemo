package com.kiven.heli.mediacodecdemo.view;

import android.content.Context;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class MySurfaceView extends SurfaceView implements SurfaceHolder.Callback{

    private SurfaceHolder holder;
    private Player player;
    private String playPath;

    public MySurfaceView(Context context, String playPath) {
        super(context);
        holder = getHolder();
        holder.addCallback(this);
        this.playPath = playPath;
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.i(this.getClass().getName(), "surface created");
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (player == null) {
            player = new Player(holder.getSurface(), playPath);
            player.start();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (player != null) {
            player.interrupt();
        }
    }
}
