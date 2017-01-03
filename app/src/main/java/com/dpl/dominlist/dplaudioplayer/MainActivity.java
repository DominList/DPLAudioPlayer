package com.dpl.dominlist.dplaudioplayer;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;


public class MainActivity extends AppCompatActivity {

    private boolean muted = false;
    private boolean isPlaying = false;
    private ImageButton buttonStop;
    private ImageButton buttonPlay;
    private ImageButton buttonRepeat;
    private ImageButton buttonMute;
    private ImageButton buttonNext;
    private ImageButton buttonPrevious;
    private TextView playlistDataView;
    private TextView trackDataView;
    private MediaPlayer mediaPlayer;
    private SongAdapter songAdapter;
    private Playlist myPlaylist;
    private boolean isPlayerLooping = false;
    private int currentSongId = 0;



    /**
     *
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        /**
         * Initialisation of variables
         */
        releaseMediaPlayer();
        myPlaylist = new Playlist();
        mediaPlayer = MediaPlayer.create(this, Uri.fromFile(new File(myPlaylist.getPlaylist().get(currentSongId).getSongPath())));
        playlistDataView = (TextView) findViewById(R.id.directory);
        trackDataView = (TextView) findViewById(R.id.current_track);
        buttonStop = (ImageButton) findViewById(R.id.button_stop);
        buttonPlay = (ImageButton) findViewById(R.id.button_play);
        buttonMute = (ImageButton)findViewById(R.id.button_mute);
        buttonRepeat = (ImageButton) findViewById(R.id.button_repeat);
        buttonNext = (ImageButton) findViewById(R.id.button_next);
        buttonPrevious = (ImageButton) findViewById(R.id.button_previous);
        ListView playlistView;

        // Set button disabled
        buttonStop.setEnabled(false);

        // show track info in trackDataView
        showTrackData();

        /**
         * Set playlist view
         */
        if (myPlaylist.getPlaylist().isEmpty()) {
            Toast.makeText(this, "No music found!", Toast.LENGTH_LONG).show();
            buttonPlay.setEnabled(false);
            buttonMute.setEnabled(false);
            buttonStop.setEnabled(false);
            buttonRepeat.setEnabled(false);
            songAdapter = null;
            playlistView = null;
        } else {
            songAdapter = new SongAdapter(this, myPlaylist.getPlaylist());
            playlistView = (ListView) findViewById(R.id.list);
            playlistView.setAdapter(songAdapter);
            playlistDataView.setText(myPlaylist.getPlaylist().size()
                    + " songs loaded from: " + myPlaylist.getMEDIA_PATH());
        }

        /**
         * Button Play Handling
         */
        buttonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mediaPlayer == null) {
                    mediaPlayer = MediaPlayer.create(MainActivity.this,
                            Uri.fromFile(new File(myPlaylist.getPlaylist().get(currentSongId).getSongPath())));
                    playMedia();
                } else if(isPlaying==false) {
                    playMedia();
                } else {
                    pauseMedia();
                }
                showTrackData();
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
         * Button Repeat Handling
         */
        buttonRepeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayer.isLooping()) {
                    repeatOff();
                } else {
                    repeatOn();
                }
            }
        });

        /**
         * Button Next Handling
         */
        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playNext();
            }
        });

        /**
         * Button Previous Handling
         */
        buttonPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playPrevious();
            }
        });

        /**
         * songListView - performing reaction on click
         */
        playlistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Song song = myPlaylist.getPlaylist().get(i);
                releaseMediaPlayer();
                mediaPlayer = MediaPlayer.create(MainActivity.this, Uri.fromFile(new File(song.getSongPath())));
                playMedia();
                if (isPlayerLooping) {
                    mediaPlayer.setLooping(true);
                }
                currentSongId = i;
                showTrackData();
            }
        });

        /**
         * Method is invoked when playback is finished
         */
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                onMediaPlaybackCompletion();
                playNext();
            }
        });
    }

    private void showTrackData() {
        Song currentSong = myPlaylist.getPlaylist().get(currentSongId);
        trackDataView.setText("Now: " + (1 + currentSong.getIndexOnPlayslist()) + ". " + currentSong.getTitle());
    }


    /**
     * Repeat on
     */
    private void repeatOn() {
        mediaPlayer.setLooping(true);
        buttonRepeat.setImageResource(R.drawable.ic_repeat_on);
        isPlayerLooping = true;
    }

    /**
     * Repeat on
     */
    private void repeatOff() {
        mediaPlayer.setLooping(false);
        buttonRepeat.setImageResource(R.drawable.ic_repeat_off);
        isPlayerLooping = false;
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

        mediaPlayer.start();
        buttonStop.setEnabled(true);
        buttonPlay.setImageResource(R.drawable.ic_pause_circle_outline_white_48dp);
        buttonMute.setEnabled(true);
        buttonRepeat.setEnabled(true);
        isPlaying=true;
    }

    /**
     * Play next song if exists
     */
    private void playNext() {
        if ((currentSongId < myPlaylist.getPlaylist().size() - 1) && (mediaPlayer.isPlaying())) {
            currentSongId++;
            releaseMediaPlayer();
            mediaPlayer = MediaPlayer.create(this,
                    Uri.fromFile(new File(myPlaylist.getPlaylist().get(currentSongId).getSongPath())));
            playMedia();
            showTrackData();
        }
    }

    /**
     * Play previous song if exists
     */
    private void playPrevious() {
        if ((currentSongId > 0) && (mediaPlayer.isPlaying())) {
            currentSongId--;
            releaseMediaPlayer();
            mediaPlayer = MediaPlayer.create(this,
                    Uri.fromFile(new File(myPlaylist.getPlaylist().get(currentSongId).getSongPath())));
            playMedia();
            showTrackData();
        }

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
        buttonRepeat.setEnabled(false);
        isPlaying=false;
    }

    /**
     * Clean up mediaPlayer by releasing its resources
     */
    private void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
            // Toast.makeText(MainActivity.this, "I'm done! :-)", Toast.LENGTH_SHORT).show();
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
        buttonRepeat.setEnabled(false);
    }

}
