package com.dpl.dominlist.dplaudioplayer;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import com.dpl.dominlist.dplaudioplayer.AudioPlayerService.MusicBinder;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.Thing;


public class MainActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {

    // Declaration of variables

    private boolean muted = false;
    private boolean isPlayerLooping = false;
    private boolean isPlayingShuffle = false;
    private boolean isPlaying = false;
    boolean isPlayerPaused = false;
    boolean isClickedOnList = false;
    // private long currentSongId;// id of current song in the player
    private int currentSongPos = 0;// position of current song on the playlist

    // Declaration of Views
    private ListView playlistView;
    private ImageButton buttonPlay;
    private ImageButton buttonRepeat;
    private ImageButton buttonMute;
    private ImageButton buttonNext;
    private ImageButton buttonPrevious;
    private ImageButton buttonForward;
    private ImageButton buttonBack;
    private ImageButton buttonShuffle;
    private TextView dirView;
    private TextView trackDataView;
    private TextView trackTimeView;
    private TextView trackDurationView;
    private SeekBar seekBar;
    private SeekBar volumeBar = null;
    private FrameLayout volumeFrame;

    private AudioManager audioManager = null;
    // Tools converting time format
    Tools utils = new Tools();
    // Adapter for playlist view
    private SongAdapter songAdapter;
    // Playlist
    private Playlist myPlaylist;
    // Declared for interaction with Runnable mUpdateTask
    private Handler handler = new Handler();
    // declaration of service handling MediaPlayer
    private AudioPlayerService audioService;

    // To bound and work with audioPlayerService
    private Intent playIntent;
    protected boolean musicBound = false;


    /**
     * currentSongPos setter for inner classes and overrides
     * @param position int
     */
    public void setCurrentSongPos(int position) {
        currentSongPos = position;
    }

    // Use when it's becoming noisy!
    private IntentFilter intentFilterNoisy = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
    private BecomingNoisyReceiver myNoisyAudioStreamReceiver = new BecomingNoisyReceiver();


    /**
     * Background Runnable thread to update progress song bar, timer and track info
     */
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {

            long totalDuration = myPlaylist.get(currentSongPos).getDuration();
            long currentDuration = audioService.getCurrentPosition();
            setCurrentSongPos(audioService.getCurrentSongPos());
            showTrackData();
            //Displaying Total Duration time
            trackDurationView.setText("" + utils.milliSecondsToTimer(totalDuration));
            // Displaying time completed playing
            trackTimeView.setText(utils.milliSecondsToTimer(currentDuration));
            // Updating progress bar
            int progress = utils.getProgressPercentage(currentDuration, totalDuration);
            //Log.d("Progress", ""+progress);
            seekBar.setProgress(progress);
            // Running this thread after 100 milliseconds
            handler.postDelayed(this, 500);
        }
    };


    /**
     * Thread hiding volumeBar
     */
    private Runnable hideVolumeBar = new Runnable() {
        @Override
        public void run() {
            volumeBar.setVisibility(View.GONE);
            handler.removeCallbacks(hideVolumeBar);
        }
    };

    /**
     * Method to control audio volume from a custom seek bar.
     * It's listening if volumeBar progress was changed.
     */
    private void initControls() {
        try {
            volumeBar = (SeekBar) findViewById(R.id.volume_bar);
            audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            volumeBar.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
            volumeBar.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));

            volumeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onStopTrackingTouch(SeekBar arg0) {
                }

                @Override
                public void onStartTrackingTouch(SeekBar arg0) {
                }

                @Override
                public void onProgressChanged(SeekBar arg0, int progress, boolean arg2) {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                            progress, 0);
                    if (progress == 0) {
                        buttonMute.setImageResource(R.drawable.ic_volume_off_white_48dp);
                    } else {
                        buttonMute.setImageResource(R.drawable.ic_volume_up_white_48dp);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Method overrides volume hardware buttons behaviour while activity is on the screen.
     *
     * @param event parameter of superclass
     * @return return of superclass
     */
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        boolean result;
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        volumeBar.setMax(maxVolume);
        Log.v("VolumeStream", "Before change: currentVolume = " + currentVolume + " maxVolume = " + maxVolume);
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    volumeBar.setVisibility(View.VISIBLE);
                    volumeBar.setProgress(++currentVolume);
                    handler.postDelayed(hideVolumeBar, 2000);

                }
                result = true;
                break;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    volumeBar.setVisibility(View.VISIBLE);
                    volumeBar.setProgress(--currentVolume);
                    handler.postDelayed(hideVolumeBar, 2000);
                }
                result = true;
                break;
            default:
                result = super.dispatchKeyEvent(event);
                break;
        }
        // Log.v("VolumeStream", "After change: currentVolume = "+currentVolume+" maxVolume = "+maxVolume);
        return result;
    }


    /**
     * On activity create
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // set volume control of music stream from the app activity
        // even if the music is not playing
        setVolumeControlStream(AudioManager.STREAM_MUSIC);


        // set volumeBar control
        initControls();

        // keep screen awake
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Init views
        dirView = (TextView) findViewById(R.id.directory);
        trackDataView = (TextView) findViewById(R.id.current_track);
        trackTimeView = (TextView) findViewById(R.id.time_text);
        trackDurationView = (TextView) findViewById(R.id.durationText);
        buttonPlay = (ImageButton) findViewById(R.id.button_play);
        buttonMute = (ImageButton) findViewById(R.id.button_mute);
        buttonRepeat = (ImageButton) findViewById(R.id.button_repeat);
        buttonForward = (ImageButton) findViewById(R.id.button_forward);
        buttonBack = (ImageButton) findViewById(R.id.button_back);
        buttonNext = (ImageButton) findViewById(R.id.button_next);
        buttonPrevious = (ImageButton) findViewById(R.id.button_previous);
        buttonShuffle = (ImageButton) findViewById(R.id.button_shuffle);
        playlistView = (ListView) findViewById(R.id.list);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        volumeFrame = (FrameLayout) findViewById(R.id.volume_bar_frame);


        // Set buttons disabled "on create" MainActivity
        //buttonStop.setVisibility(View.GONE);
        buttonBack.setEnabled(false);
        buttonForward.setEnabled(false);
        buttonNext.setEnabled(false);
        buttonPrevious.setEnabled(false);
        seekBar.setEnabled(false);
        seekBar.setProgress(0);
        seekBar.setMax(100);

        /**
         * Set playlist view
         */
        myPlaylist = getPlaylist();
        if (!myPlaylist.isEmpty()) {
            songAdapter = new SongAdapter(this, myPlaylist);
            playlistView.setAdapter(songAdapter);
            dirView.setText(myPlaylist.size() + " songs loaded");
            // set seekBar to check if moves
            showTrackData();
            seekBar.setOnSeekBarChangeListener(this);
            // audioService.setMediaPlayer();
        } else {
            dirView.setText(R.string.no_music_found);
            trackDataView.setText("");
            buttonPlay.setEnabled(false);
            seekBar.setEnabled(false);
        }

        /**
         * Button Play Handling
         */
        buttonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (audioService.isPlaying()) {
                    pauseSong();
                } else if (isPlayerPaused) {
                    resumeSong();
                } else {
                    playSong();
                }
                isClickedOnList = false;
            }
        });


        /**
         * Button Mute Handling
         */
        buttonMute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (volumeBar.getVisibility() == View.VISIBLE) {
                    // mediaMuteOff();
                    volumeBar.setVisibility(View.GONE);

                } else {
                    // mediaMuteOn();
                    volumeBar.setVisibility(View.VISIBLE);
                }

            }
        });

        /**
         * Button Repeat Handling
         */
        buttonRepeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isPlayerLooping) {
                    repeatOff();
                    isPlayerLooping = false;
                } else {
                    repeatOn();
                    isPlayerLooping = true;
                }
            }
        });

        /**
         * Button shuffle on click
         */
        buttonShuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isPlayingShuffle && audioService.isShuffle()) {
                    buttonShuffle.setImageResource(R.drawable.ic_action_shuffle_off);
//                    Toast.makeText(MainActivity.this, R.string.shuffle_off, Toast.LENGTH_SHORT).show();
                    isPlayingShuffle = false;
                    audioService.setShuffle(false);
                } else {
                    buttonShuffle.setImageResource(R.drawable.ic_action_shuffle_on);
//                    Toast.makeText(MainActivity.this, R.string.shuffle_on, Toast.LENGTH_SHORT).show();
                    isPlayingShuffle = true;
                    audioService.setShuffle(true);
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
                isClickedOnList = false;
            }
        });

        /**
         * Button Previous Handling
         */
        buttonPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playPrev();
                isClickedOnList = false;
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
         * Button back on click
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
                handler.removeCallbacks(mUpdateTimeTask);
                currentSongPos = i;
                isClickedOnList = true;
                playSong();
                updateProgressBar();
            }
        });

    }

    /**
     * On start activity
     */
    @Override
    protected void onStart() {
        super.onStart();// ATTENTION: This was auto-generated to implement the App Indexing API.
// See https://g.co/AppIndexing/AndroidStudio for more information.
        if (playIntent == null) {
            playIntent = new Intent(this, AudioPlayerService.class);
            bindService(playIntent, musicConnection, BIND_AUTO_CREATE);
            startService(playIntent);
        }

    }

    /**
     * On destroy activity
     */
    @Override
    protected void onDestroy() {
        handler.removeCallbacks(mUpdateTimeTask);
        stopService(playIntent);
        audioService = null;
        super.onDestroy();
    }

    /**
     * Do when AudioPlayerService is connected or disconnected
     */
    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MusicBinder binder = (MusicBinder) iBinder;
            audioService = binder.getService();
            audioService.initPlaylist(myPlaylist);
            audioService.setMediaPlayer();
            MainActivity.this.musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

            MainActivity.this.musicBound = false;
        }
    };

    /**
     * Retrieve playlist from android database
     *
     * @return Playlist() object
     */
    private Playlist getPlaylist() {
        Playlist thisPlaylist = new Playlist();
        ContentResolver musicResolver = getContentResolver();
        Cursor musicCursor = musicResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, MediaStore.Audio.Media.IS_MUSIC + "==1", null, null);

        if (musicCursor != null && musicCursor.moveToFirst()) {
            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.ARTIST);
            int albumColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
            int durationColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DURATION);

            //add songs to list
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                String thisAlbum = musicCursor.getString(albumColumn);
                int thisDuration = musicCursor.getInt(durationColumn);
                Song tempSong = new Song();
                tempSong.setArtist(thisArtist);
                tempSong.setTitle(thisTitle);
                tempSong.setAlbum(thisAlbum);
                tempSong.setId(thisId);
                tempSong.setDuration(thisDuration);
                thisPlaylist.add(tempSong);
            } while (musicCursor.moveToNext());
        }
        musicCursor.close();
        return thisPlaylist;
    }

    /**
     * Play next song
     */
    private void playNext() {
        handler.removeCallbacks(mUpdateTimeTask);
        audioService.setMediaPlayer();
        audioService.playNext();
        // controller.show(0);
        currentSongPos = audioService.getCurrentSongPos();
        isPlayerPaused = false;
        if (!isPlayerPaused) updateProgressBar();
    }

    /**
     * Play previous song
     */
    private void playPrev() {
        handler.removeCallbacks(mUpdateTimeTask);
        audioService.setMediaPlayer();
        audioService.playPrev();
        // controller.show(0);
        currentSongPos = audioService.getCurrentPosition();
        isPlayerPaused = false;
        seekBar.setProgress(0);
        trackTimeView.setText(R.string.time_reset);
        if (!isPlayerPaused) updateProgressBar();
    }

    /**
     * Show track data in the textView
     */
    private void showTrackData() {
        Song currentSong = myPlaylist.get(currentSongPos);
        trackDataView.setText(currentSong.getArtist() + " - " + currentSong.getTitle());
    }

    /**
     * Repeat on
     */
    private void repeatOn() {
        audioService.setLooping(true);
        buttonRepeat.setImageResource(R.drawable.ic_repeat_on);
    }

    /**
     * Repeat on
     */
    private void repeatOff() {
        audioService.setLooping(false);
        buttonRepeat.setImageResource(R.drawable.ic_repeat_off);
    }

    /**
     * Mute off
     */
    private void mediaMuteOff() {
        //audioService.muteOff();
        muted = false;
        //buttonMute.setImageResource(R.drawable.ic_volume_up_white_48dp);
    }

    /**
     * On media mute
     */
    private void mediaMuteOn() {
        //audioService.muteOn();
        muted = true;
        //buttonMute.setImageResource(R.drawable.ic_volume_off_white_48dp);
    }

    /**
     * Play media
     */
    private void playSong() {
        if (isPlayingShuffle && !isClickedOnList) {
            audioService.setShuffledSongPos();
        } else {
            audioService.setSong(currentSongPos);
        }
        audioService.setMediaPlayer();
        audioService.playSong();
        seekBar.setProgress(0);
        seekBar.setMax(100);
        seekBar.setEnabled(true);
        buttonPlay.setImageResource(R.drawable.ic_pause_circle_outline_white_48dp);
        buttonMute.setEnabled(true);
        buttonRepeat.setEnabled(true);
        buttonBack.setEnabled(true);
        buttonForward.setEnabled(true);
        buttonNext.setEnabled(true);
        buttonPrevious.setEnabled(true);
        isPlaying = true;
        isPlayerPaused = false;
        Log.v("MainActivity", "playSong: currentSongPos= " + currentSongPos);
        updateProgressBar();
        registerReceiver(myNoisyAudioStreamReceiver, intentFilterNoisy);

    }

    /**
     * This method run player only in case of paused state
     */
    private void resumeSong() {
        audioService.resumeSong();
        buttonPlay.setImageResource(R.drawable.ic_pause_circle_outline_white_48dp);
        buttonMute.setEnabled(true);
        buttonRepeat.setEnabled(true);
        buttonBack.setEnabled(true);
        buttonForward.setEnabled(true);
        isPlaying = true;
        isPlayerPaused = false;
        updateProgressBar();
        Log.v("MainActivity", "resumeSong: currentSongPos= " + currentSongPos);
        registerReceiver(myNoisyAudioStreamReceiver, intentFilterNoisy);
    }


    /**
     * Seek to time in song forward
     */
    private void goForward() {
        audioService.seekTo(audioService.getCurrentPosition() + 3000);
    }

    /**
     * Seek to time in song backwards
     */
    private void goBack() {
        audioService.seekTo(audioService.getCurrentPosition() - 3000);
    }

    /**
     * Pause media
     */
    private void pauseSong() {
        handler.removeCallbacks(mUpdateTimeTask);
        audioService.pauseSong();
        buttonPlay.setImageResource(R.drawable.ic_play_circle_outline_white_48dp);
        buttonPlay.setEnabled(true);
        isPlayerPaused = true;
        updateProgressBar();
        unregisterReceiver(myNoisyAudioStreamReceiver);
    }

    /**
     * Stop media
     */
    private void stopMedia() {
        handler.removeCallbacks(mUpdateTimeTask);
        audioService.stopSong();
        mediaMuteOff();
        buttonPlay.setImageResource(R.drawable.ic_play_circle_outline_white_48dp);
        buttonMute.setEnabled(false);
        buttonRepeat.setEnabled(false);
        buttonNext.setEnabled(false);
        buttonPrevious.setEnabled(false);
        buttonBack.setEnabled(false);
        buttonForward.setEnabled(false);
        seekBar.setEnabled(true);
        seekBar.setProgress(0);
        seekBar.setEnabled(false);
        trackTimeView.setText(R.string.time_reset);
        isPlaying = false;
        isPlayerPaused = false;
    }


    /*
        The block below is used to handle seekBar reactions.
     */

    /**
     * Handling the thread of mUpdateTimeTask in the main thread
     */
    public void updateProgressBar() {
        handler.postDelayed(mUpdateTimeTask, 200);
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
     * When user stops moving the progress handler
     */
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        int totalDuration = audioService.getDuration();
        int currentPosition = utils.progressToTimer(seekBar.getProgress(), totalDuration);

        // forward or backward to certain seconds
        audioService.seekTo(currentPosition);

        // update timer progress again
        updateProgressBar();
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    /**
     * Becoming noisy broadcast receiver
     */
    private class BecomingNoisyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                pauseSong();
            }
        }
    }


}