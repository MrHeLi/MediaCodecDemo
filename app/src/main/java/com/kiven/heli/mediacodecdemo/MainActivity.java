package com.kiven.heli.mediacodecdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.kiven.heli.mediacodecdemo.view.MySurfaceView;

public class MainActivity extends AppCompatActivity {

    private MySurfaceView playerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String playSource = getPlaySource();
        playerView = new MySurfaceView(this , playSource);
        setContentView(playerView);
    }

    private String getPlaySource() {
        ///sdcard/Download/legao.flv
        return "/sdcard/Download/legao.flv";
    }

}
