package com.dpl.dominlist.dplaudioplayer;

import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AudioPlayerService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnSeekCompleteListener
{

    // Our player declaration
    private MediaPlayer mediaPlayer;
    // Our playlist declaration
    private Playlist playlist;
    // Start position on the playlist
    private int currentSongPos = 0;
    // Binder of current service class
    private IBinder musicBinder = new MusicBinder();
    // Field to recognize if song is or should be playing, is false on pause and stop
    private boolean songIsPlaying = false;
    // Play shuffled field
    private boolean shuffle = false;
    // Play song in loop field
    private boolean looping = false;
    // array of positions must be discarded from shuffle queue
    private List<Integer> discardShuffle = new ArrayList<>();
    // AudioManager
    AudioManager audioManager;

    // Handler to control runnable
    // Managing AudioFocus
    AudioManager.OnAudioFocusChangeListener afChangeListener =
            new AudioManager.OnAudioFocusChangeListener() {
                public void onAudioFocusChange(int focusChange) {
                    if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                        // Permanent loss of audio focus
                         pauseSong();    // Pause playback immediately
                        //MediaControllerCompat mediaController;
                        //mediaController.getTransportControls().pause();
                        // Wait 30 seconds before stopping playback
                       // mHandler.postDelayed(mDelayedStopRunnable, 30,000L);
                    } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                        // Pause playback
                        mediaPlayer.pause(); // keeps flag isSongPlaying untouched
                    } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
                        mediaPlayer.setVolume(0.5F, 0.5F);
                    } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                        // Your app has been granted audio focus again
                        // Raise volume to normal, restart playback if necessary
                        mediaPlayer.setVolume(1F, 1F);
                        resumeSong();
                    }
                }
            };


    /**
     * Check if
     * @return boolean shuffle for automatic player changes.
     */
    public boolean isShuffle() {
        return shuffle;
    }

    /**
     * Set if playlist should be shuffled.
     * @param shuffle boolean value for automatic song changes.
     */
    public void setShuffle(boolean shuffle) {
        this.shuffle = shuffle;
    }

    /**
     * Methode sets random number to currentSongPos to choose another unique song
     * @return false if all the song have been played
     */
    public boolean setShuffledSongPos() {
        Random shufflePos = new Random(); // declare Random instance
        discardShuffle.add(Integer.valueOf(currentSongPos)); // Add current song position to array
        do {
            currentSongPos = shufflePos.nextInt(playlist.size());
            Log.v("Random position"," ="+currentSongPos );
        } while (discardShuffle.contains(Integer.valueOf(currentSongPos)) &&
                discardShuffle.size() < playlist.size() );

        Log.v("AudioPlayerService", "Added to shuffleDiscard: currentSongPos ="+ currentSongPos+
        " discardShuffle.size()=" + discardShuffle.size() +" of\n"+ discardShuffle);
        return discardShuffle.size() == playlist.size() ? false : true;
    }


    /**
     * Set position of current song in
     * @param songIndex to setMediaPlayer
     */
    public void setSongIndex(int songIndex){
        currentSongPos=songIndex;
    }


    /**
     *
     * @return int position on the playlist of the current song
     *
     */
    public int getCurrentSongPos(){
        Log.v("getCurrentSongPos", "currentSongPos= "+ currentSongPos);
        return currentSongPos;

    }

    /**
     *  Play song in a service
     */
    public void playSong() {
        isLooping();
        songIsPlaying=true;
        mediaPlayer.prepareAsync();
        Log.v("AudioPlayerService", "currentSongPos= "+ currentSongPos);

    }

    /**
     * set current song to MediaPlayer instance
     */
    public void setMediaPlayer(){
        mediaPlayer.reset();
        Song currentSong = playlist.get(currentSongPos);
        long currentSongID = currentSong.getId();
        // get the URI for local audio
        Uri trackUri = ContentUris.withAppendedId(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, currentSongID );
        // try to set the media to the player
        try{
            mediaPlayer.setDataSource(getApplicationContext(), trackUri);
        } catch (Exception e){
            Log.e("AUDIO PLAYER SERVICE", "Error setting data source", e);
        }
        Log.v("setMediaPlayer", "currentSongPos= "+ currentSongPos);

    }

    /**
     * Resume to play from pause state
     */
    public void resumeSong(){
        requestFocusAndPlay(mediaPlayer);
        songIsPlaying = true;

    }

    /**
     * Set player to currentSongPos and continues to play
     */
    private void setAndPlayOnPlaying(){
        if (songIsPlaying) {
            setMediaPlayer();
            playSong();
        }
    }

    /**
     *  Method plays previous song from playlist
     */
    public void playPrev() {
        if (currentSongPos>0 && !shuffle) {
            --currentSongPos;
            setAndPlayOnPlaying();
            Log.v("AudioPlayerService", "playPrev(): We are back-counting");
        }else if (shuffle){
            if (!discardShuffle.isEmpty() && discardShuffle != null) {
                // get last element of discardedShuffle and set it as current song
                currentSongPos = discardShuffle.get(discardShuffle.size() - 1);
                // remove las object
                discardShuffle.remove(discardShuffle.size()-1);
           } // currentPos stay the same

            setAndPlayOnPlaying();
            isLooping();
            Log.v("AudioPlayerService", "playPrev(): discardShuffle SIZE ="
                    + discardShuffle.size()+"\n"+discardShuffle);
        }
    }

    /**
     * Method plays next song from playlist
     */
    public void playNext() {
        if ((currentSongPos < playlist.size()-1) && !shuffle ){
            ++currentSongPos;
            setAndPlayOnPlaying();
            Log.v("AudioPlayerService", "playNext(): We are next-counting");
        }else if (shuffle){
            if(!setShuffledSongPos()) { // if discardShuffle includes each song numbers of playlist
                // clear discardShuffle and choose another random number
                discardShuffle.clear();
                Log.v("AudioPlayerService", "cleared: discardShuffle = " + discardShuffle);
                setShuffledSongPos();
            }
            setAndPlayOnPlaying();
            isLooping();
            Log.v("AudioPlayerService", "playNext(): We are  next-shuffling");
        }
    }

    /**
     * Method to pause current playing
     */
    public void pauseSong(){
        mediaPlayer.pause();
        songIsPlaying=false;
        audioManager.abandonAudioFocus(afChangeListener);
    }

    /**
     * Method to stop the mediaPlayer
     */
    public void stopSong(){
        mediaPlayer.stop();
        songIsPlaying = false;
    }

    // TODO: Rebuild these methods to be one setMute(boolean value)

    public void muteOn(){
        mediaPlayer.setVolume(0,0);
    }

    public void muteOff(){
        mediaPlayer.setVolume(1,1);
    }
    
    /**
     * Seek to position ina a song.
     * @param ms time position of played in milliseconds
     */
    public void seekTo(int ms){
        mediaPlayer.seekTo(ms);
    }

    /**
     *
     * @return current song playback position in milliseconds
     */
    public int getCurrentPosition(){
        return mediaPlayer.getCurrentPosition();
    }

    /**
     *
     * @return total duration of current media file in milliseconds
     */
    public int getDuration() {
        return mediaPlayer.getDuration();
    }

    /**
     * On Service create.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = new MediaPlayer();
        initMediaPlayer();
    }

    /**
     * On bind Service
     * @param intent of service binder
     * @return musicBinder
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return musicBinder;
    }

    /**
     *
     * @param intent of service  binder
     * @return false when is unbind
     */
    @Override
    public boolean onUnbind(Intent intent) {
        mediaPlayer.stop();
        mediaPlayer.release();
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        audioManager.abandonAudioFocus(afChangeListener);
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if (shuffle && !isLooping()) {
           setShuffledSongPos();
        }
        setMediaPlayer();
        playNext();
        Log.v("OnCompletion", "currentSongPos= "+ currentSongPos);
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        mediaPlayer.reset();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        requestFocusAndPlay(mp);
    }

    /**
     * Request audio focus when start playing.
     * @param mp of current MediaPlayer
     */
    public void requestFocusAndPlay(MediaPlayer mp){
        audioManager = (AudioManager)
                AudioPlayerService.this.getSystemService(getBaseContext().AUDIO_SERVICE);

        int result = audioManager.requestAudioFocus
                (afChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            mp.start();
        } else if (result == AudioManager.AUDIOFOCUS_REQUEST_FAILED) {
            Toast.makeText(AudioPlayerService.this, "AudioFocus Failed!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Playlist initialisation
     * @param mPlaylist of Playlist type should be passed as new playlist.
     */
    public void initPlaylist(Playlist mPlaylist) {
        playlist =  mPlaylist;
        Log.v("Playlist:","");
        for(Song song: playlist) {
            Log.v("Before sort: Song", song.toString());
        }
    }

    /**
     *
     * @return MediaPlayer.isPlaying in the service
     */
    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    /**
     * Set looping the mediaPlayer
     * @param value boolean
     */
    public void setLooping(boolean value) {
        mediaPlayer.setLooping(value);
        looping = value;
    }

    /**
     * Method check the value of looping field in the Service class
     * and sets it to the current instance of mediaPlayer.
     * @return MediaPlayer.isLooping() in the service
     */
    public boolean isLooping(){
        mediaPlayer.setLooping(looping);
        return mediaPlayer.isLooping();
    }

    /**
     * Initialisation of mediaPlayer
     * Setting wake mode to play music when the screen is off.
     */
    public void initMediaPlayer() {
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setScreenOnWhilePlaying(true);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnSeekCompleteListener(this);


    }

    /**
     * Method to perform within OnSeekCompletionListener,
     * when mediaPlayer method seekTo is completed
     * @param mp is current instance of MediaPlayer
     */
    @Override
    public void onSeekComplete(MediaPlayer mp) {
        if(mediaPlayer.isPlaying())  mp.start();
    }

    /**
     * Inner class to bind this service with activity
     */
    class MusicBinder extends Binder {
        public AudioPlayerService getService() {
            return AudioPlayerService.this;
        }
    }


}
