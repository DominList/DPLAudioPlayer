package com.dpl.dominlist.dplaudioplayer;

import android.content.Context;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by dominlist on 21.12.2016.
 */

public class Playlist extends ArrayList<Song>{

    private  List<Song> playlist = new ArrayList<>();


    public List<Song> Playlist() {
        return playlist;
    }

    public void setPlaylist(Playlist mPlaylist){
        playlist = mPlaylist;
    }

    /**
     *  sort playlist
     */
    public void sort() {
        Collections.sort(playlist);
        // Put songs to log in order:
//        for (Song song : playlist) {
//            Log.v("Song sorted:", song.getTitle());
//        }
    }
}