package com.dpl.dominlist.dplaudioplayer;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    private boolean muted = false;
    private boolean isPlaying = false;
    private ImageButton buttonStop;
    private ImageButton buttonPlay;
    private ImageButton buttonMute;
    private TextView songDataText;
    private MediaPlayer mediaPlayer;


    /**
     *
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        releaseMediaPlayer();
        mediaPlayer = MediaPlayer.create(this, R.raw.music);
        songDataText = (TextView)findViewById(R.id.song_text);
        buttonStop = (ImageButton)findViewById(R.id.button_pause);
        buttonPlay = (ImageButton) findViewById(R.id.button_play);
        buttonMute = (ImageButton)findViewById(R.id.button_mute);


        buttonStop.setEnabled(false);

        /**
         * Button Play Handling
         */
        buttonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mediaPlayer == null) {
                    mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.music);
                    playMedia();
                } else if(isPlaying==false) {
                    playMedia();
                } else {
                    pauseMedia();
                }
            }
        });

        /**
         * Button Stop Handling
         */
        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopMedia();
            }
        });

        /**
         * Button Mute Handling
         */
        buttonMute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(muted) {
                    mediaMuteOff();
                } else {
                    mediaMute();
                }
            }
        });

        /**
         * Method is invoked when playback is finished
         */
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                onMediaPlaybackCompletion();
            }
        });
    }

    /**
     * Mute off
     */
    private void mediaMuteOff(){
        mediaPlayer.setVolume(1,1);
        muted = false;
        buttonMute.setImageResource(R.drawable.ic_volume_up_white_48dp);
    }

    /**
     * On media mute
     */
    private void mediaMute(){
        mediaPlayer.setVolume(0, 0);
        muted = true;
        buttonMute.setImageResource(R.drawable.ic_volume_off_white_48dp);
        Toast.makeText(MainActivity.this, R.string.mute , Toast.LENGTH_SHORT).show();
    }

    /**
     * Play media
     */
    private void playMedia(){
        songDataText.setText("Anna Jurksztowicz - Stan Pogody");
        mediaPlayer.start();
        buttonStop.setEnabled(true);
        buttonPlay.setImageResource(R.drawable.ic_pause_circle_outline_white_48dp);
        buttonMute.setEnabled(true);
        isPlaying=true;
    }

    /**
     * Pause media
     */
    private void pauseMedia(){
        buttonPlay.setImageResource(R.drawable.ic_play_circle_outline_white_48dp);
        mediaPlayer.pause();
        buttonStop.setEnabled(true);
        buttonPlay.setEnabled(true);
        isPlaying=false;
    }

    /**
     * Stop media
     */
    private void stopMedia(){
        mediaPlayer.stop();
        mediaMuteOff();
        releaseMediaPlayer();
        buttonPlay.setImageResource(R.drawable.ic_play_circle_outline_white_48dp);
        buttonStop.setEnabled(false);
        buttonMute.setEnabled(false);
        isPlaying=false;
        songDataText.setText("");
    }

    /**
     * Clean up mediaPlayer by releasing its resources
     */
    private void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
            Toast.makeText(MainActivity.this, "I'm done! :-)", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * On media playback completion
     */
    private void onMediaPlaybackCompletion() {
        releaseMediaPlayer();
        buttonPlay.setEnabled(true);
        buttonStop.setEnabled(false);
        buttonMute.setEnabled(false);
    }

}
