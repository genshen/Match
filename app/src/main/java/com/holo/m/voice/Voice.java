package com.holo.m.voice;

import android.content.Context;
import android.media.MediaPlayer;

import java.io.IOException;

/**
 * Created by 根深 on 2016/2/8.
 */
public class Voice {
    public static String BASE_PATH = "/sdcard/Music/";
    protected static boolean isPlaying = false;
    protected static MediaPlayer mediaPlayer = new MediaPlayer();

    public static void playVoice(Context context, String file_name) {
        try {
            if (isPlaying) {
                mediaPlayer.stop();
                isPlaying = false;
            } else {
                mediaPlayer.reset();
                mediaPlayer.setDataSource(BASE_PATH + file_name);
                mediaPlayer.prepare();
                mediaPlayer.start();
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        isPlaying = false;
                    }
                });
                isPlaying = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
