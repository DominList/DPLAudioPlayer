package com.dpl.dominlist.dplaudioplayer;


import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by dominlist on 21.12.2016.
 */

public class Playlist {

    private static List<Song> playlist = new ArrayList<>();
    /**
     * parameters
     */
    private String MEDIA_PATH = "/sdcard/music";

    /**
     * Class constructor
     */
    public Playlist() {
        setPlaylistFromPath(MEDIA_PATH);
    }

    /**
     * Reload files from default directory
     */
    public void reloadPlaylist() {
        setPlaylistFromPath(MEDIA_PATH);
    }

    /**
     * Getter
     *
     * @return String of MEDIA_PATH
     */
    public String getMEDIA_PATH() {
        return MEDIA_PATH;
    }

    /**
     * Looking for music files in music directory
     *
     * @param mMEDIA_PATH represents path directory of String type
     *                    and it lets to work recursively searching in sub-folders.
     *                    The method is passing filtered list of files to the playlist parameter
     *                    of ArrayList.
     */
    private void setPlaylistFromPath(String mMEDIA_PATH) {
        File music = new File(mMEDIA_PATH);
        // Log.v("SongList", music.list().toString());
        if (music.listFiles().length > 0) {
            for (File file : music.listFiles()) {
                if (file.isDirectory()) {
                    Log.v("AbsolutePath", file.getPath());
                    setPlaylistFromPath(file.getAbsolutePath());
                } else if (file.getName().toLowerCase().endsWith(".mp3") ||
                        file.getName().toLowerCase().endsWith(".ogg") ||
                        file.getName().toLowerCase().endsWith(".wma") ||
                        file.getName().toLowerCase().endsWith(".wav") ||   //above android 4.1!!
                        file.getName().toLowerCase().endsWith(".mp4") ||
                        file.getName().toLowerCase().endsWith(".m4a") ||
                        file.getName().toLowerCase().endsWith(".flac") ||
                        file.getName().toLowerCase().endsWith(".3gp") ||
                        file.getName().toLowerCase().endsWith(".aac")) {
                    Song song = new Song();
                    song.setTitle(file.getName());
                    song.setSongPath(file.getPath());
                    playlist.add(song);
                    song.setIndexOnPlayslist(playlist.indexOf(song));
                    Log.v("Song added:", song.toString());
                }
            }
        } else {
            Log.v("Media", "There's no media!!!");
        }
    }


    /**
     * Playlist getter
     *
     * @return the ArrayList<Songs>
     */
    public List<Song> getPlaylist() {
        return playlist;
    }

    public void sort() {
        Collections.sort(playlist);
        // Put songs to log in order:
        for (Song song : playlist) {
            Log.v("Song:", song.toString());
        }
    }

    void leaveMusicOnPlaylist() {
        playlist = new ArrayList<Song>();

    }

}