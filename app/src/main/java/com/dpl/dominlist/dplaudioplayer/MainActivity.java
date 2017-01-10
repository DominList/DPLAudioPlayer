package com.dpl.dominlist.dplaudioplayer;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;


public class MainActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {

    ListView playlistView;
    Tools utils = new Tools();
    private boolean muted = false;
    private boolean isPlaying = false;
    private boolean isPlayerLooping = false;
    private ImageButton buttonStop;
    private ImageButton buttonPlay;
    private ImageButton buttonRepeat;
    private ImageButton buttonMute;
    private ImageButton buttonNext;
    private ImageButton buttonPrevious;
    private ImageButton buttonForward;
    private ImageButton buttonBack;
    private TextView playlistDataView;
    private TextView trackDataView;
    private TextView trackTimeView;
    private SeekBar seekBar;
    private MediaPlayer mediaPlayer;
    private SongAdapter songAdapter;
    private Playlist myPlaylist;
    private int currentSongId = 0;  // id of current song in the player
    private Handler handler = new Handler(); // Declared for interaction with Runnable mUpdateTask

    /**
     * Background Runnable thread
     */
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            long totalDuration = mediaPlayer.getDuration();
            long currentDuration = mediaPlayer.getCurrentPosition();

            // Displaying Total Duration time
            //trackTimeView.setText(""+utils.milliSecondsToTimer(totalDuration));
            // Displaying time completed playing
            trackTimeView.setText("" + utils.milliSecondsToTimer(currentDuration));

            // Updating progress bar
            int progress = utils.getProgressPercentage(currentDuration, totalDuration);
            //Log.d("Progress", ""+progress);
            seekBar.setProgress(progress);

            // Running this thread after 100 milliseconds
            handler.postDelayed(this, 100);
        }
    };

    /**
     *
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // keep screen awake
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // keep cpu awake
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "MyTotalWakeLock");
        wakeLock.acquire();


        /**
         * Initialisation of variables
         */
        releaseMediaPlayer();
        myPlaylist = new Playlist();

        playlistDataView = (TextView) findViewById(R.id.directory);
        trackDataView = (TextView) findViewById(R.id.current_track);
        trackTimeView = (TextView) findViewById(R.id.time_text);
        buttonStop = (ImageButton) findViewById(R.id.button_stop);
        buttonPlay = (ImageButton) findViewById(R.id.button_play);
        buttonMute = (ImageButton) findViewById(R.id.button_mute);
        buttonRepeat = (ImageButton) findViewById(R.id.button_repeat);
        buttonForward = (ImageButton) findViewById(R.id.button_forward);
        buttonBack = (ImageButton) findViewById(R.id.button_back);
        buttonNext = (ImageButton) findViewById(R.id.button_next);
        buttonPrevious = (ImageButton) findViewById(R.id.button_previous);
        seekBar = (SeekBar) findViewById(R.id.seekBar);


        seekBar.setOnSeekBarChangeListener(this);

        // Set buttons disabled "on create" MainActivity
        buttonStop.setEnabled(false);
        buttonBack.setEnabled(false);
        buttonForward.setEnabled(false);

        // show track info in trackDataView
        showTrackData();

        /**
         * Set playlist view
         */
        if (myPlaylist.getPlaylist().isEmpty()) {
            Toast.makeText(this, "No music found!", Toast.LENGTH_LONG).show();
            mediaPlayer = null;
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
            mediaPlayer = MediaPlayer.create(this,
                    Uri.fromFile(new File(myPlaylist.getPlaylist().get(currentSongId).getSongPath())));
        }

        /**
         * Button Play Handling
         */
        buttonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayer == null) {
                    mediaPlayer = MediaPlayer.create(MainActivity.this,
                            Uri.fromFile(new File(myPlaylist.getPlaylist().get(currentSongId).getSongPath())));
                    playSong();
                } else if (isPlaying == false) {
                    playSong();
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
                if (muted) {
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
         * Button forward click
         */
        buttonForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goForward();
            }
        });

        /**
         * Button back click
         */
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goBack();
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
                playSong();
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
                playSong();
            }
        });
    }

    /**
     * On media playback completion
     */
    private void onMediaPlaybackCompletion() {
        stopMedia();
        buttonPlay.setEnabled(true);
        buttonStop.setEnabled(false);
        buttonMute.setEnabled(false);
        buttonRepeat.setEnabled(false);
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
        Toast.makeText(this, R.string.repeat_on, Toast.LENGTH_SHORT).show();
    }

    /**
     * Repeat on
     */
    private void repeatOff() {
        mediaPlayer.setLooping(false);
        buttonRepeat.setImageResource(R.drawable.ic_repeat_off);
        isPlayerLooping = false;
        Toast.makeText(this, R.string.repeat_off, Toast.LENGTH_SHORT).show();
    }

    /**
     * Mute off
     */
    private void mediaMuteOff() {
        mediaPlayer.setVolume(1, 1);
        muted = false;
        buttonMute.setImageResource(R.drawable.ic_volume_up_white_48dp);
    }

    /**
     * On media mute
     */
    private void mediaMute() {
        mediaPlayer.setVolume(0, 0);
        muted = true;
        buttonMute.setImageResource(R.drawable.ic_volume_off_white_48dp);
        Toast.makeText(MainActivity.this, R.string.mute, Toast.LENGTH_SHORT).show();
    }

    /**
     * Play media
     */
    private void playSong() {

        mediaPlayer.start();
        seekBar.setProgress(0);
        seekBar.setMax(100);
        seekBar.setEnabled(true);
        buttonStop.setEnabled(true);
        buttonPlay.setImageResource(R.drawable.ic_pause_circle_outline_white_48dp);
        buttonMute.setEnabled(true);
        buttonRepeat.setEnabled(true);
        buttonBack.setEnabled(true);
        buttonForward.setEnabled(true);
        isPlaying = true;
        updateProgressBar();
    }

    /**
     * Play next song if exists
     */
    private void playNext() {
        if ((currentSongId < myPlaylist.getPlaylist().size() - 1)) {
            currentSongId++;
            releaseMediaPlayer();
            mediaPlayer = MediaPlayer.create(this,
                    Uri.fromFile(new File(myPlaylist.getPlaylist().get(currentSongId).getSongPath())));
            if (isPlaying) {
                playSong();
            }
            showTrackData();
        }
    }

    /**
     * Play previous song if exists
     */
    private void playPrevious() {
        if ((currentSongId > 0)) {
            currentSongId--;
            releaseMediaPlayer();
            mediaPlayer = MediaPlayer.create(this,
                    Uri.fromFile(new File(myPlaylist.getPlaylist().get(currentSongId).getSongPath())));
            if (isPlaying) {
                playSong();
            }
            showTrackData();
        }

    }

    private void goForward() {
        mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() + 3000);
    }

    private void goBack() {
        mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() - 3000);
    }

    /**
     * Pause media
     */
    private void pauseMedia() {
        buttonPlay.setImageResource(R.drawable.ic_play_circle_outline_white_48dp);
        mediaPlayer.pause();
        buttonStop.setEnabled(true);
        buttonPlay.setEnabled(true);
        isPlaying = false;
    }

    /**
     * Stop media
     */
    private void stopMedia() {

        mediaPlayer.stop();
        updateProgressBar();
        handler.removeCallbacks(mUpdateTimeTask);
        mediaMuteOff();
        releaseMediaPlayer();
        buttonPlay.setImageResource(R.drawable.ic_play_circle_outline_white_48dp);
        buttonStop.setEnabled(false);
        buttonMute.setEnabled(false);
        buttonRepeat.setEnabled(false);
        seekBar.setEnabled(true);
        seekBar.setProgress(0);
        seekBar.setEnabled(false);
        trackTimeView.setText(R.string.time_reset);

        isPlaying = false;
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


    /*
        The block below is used to handle seekBar reactions.
     */

    /**
     * Handling the thread of mUpdateTimeTask in the main thread
     */
    public void updateProgressBar() {
        handler.postDelayed(mUpdateTimeTask, 100);
    }

    /**
     * Unused method of seekBar
     */
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
    }

    /**
     * When user starts moving the progress handler
     */
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // remove message Handler from updating progress bar
        handler.removeCallbacks(mUpdateTimeTask);
    }

    /**
     * When user stops moving the progress hanlder
     */
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        handler.removeCallbacks(mUpdateTimeTask);
        int totalDuration = mediaPlayer.getDuration();
        int currentPosition = utils.progressToTimer(seekBar.getProgress(), totalDuration);

        // forward or backward to certain seconds
        mediaPlayer.seekTo(currentPosition);

        // update timer progress again
        updateProgressBar();
    }
}



