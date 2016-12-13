package com.dpl.dominlist.dplaudioplayer;

import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private boolean muted = false;

    private Button buttonPause;
    private Button buttonPlay;
    private Button buttonMute;
    private TextView muteText;
    private TextView timeText;
    private MediaPlayer mediaPlayer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        releaseMediaPlayer();
        mediaPlayer = MediaPlayer.create(this, R.raw.music);

        buttonPause = (Button)findViewById(R.id.button_pause);
        buttonPlay = (Button)findViewById(R.id.button_play);
        buttonMute = (Button)findViewById(R.id.button_mute);
        muteText = (TextView)findViewById(R.id.mute_text);
        timeText = (TextView)findViewById(R.id.time_text);


        buttonPause.setEnabled(false);
        buttonMute.setEnabled(false);


        timeText.setText(getPlayerTime());



        buttonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mediaPlayer == null) {
                    mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.music);
                }

                mediaPlayer.start();
                buttonPause.setEnabled(true);
                buttonPlay.setEnabled(false);
                buttonMute.setEnabled(true);

                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        releaseMediaPlayer();
                        buttonPlay.setEnabled(true);
                        buttonPause.setEnabled(false);
                        buttonMute.setEnabled(false);
                    }
                });
            }
        });

        buttonPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.pause();
                buttonPause.setEnabled(false);
                buttonPlay.setEnabled(true);
            }
        });


        buttonMute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(muted) {
                    mediaPlayer.setVolume(1,1);
                    muted = false;
                    muteText.setText("");
                } else {
                    mediaPlayer.setVolume(0, 0);
                    muted = true;
                    muteText.setText(R.string.mute);
                }
            }
        });

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

    private String getPlayerTime() {
        return mediaPlayer.toString();
    }


}
