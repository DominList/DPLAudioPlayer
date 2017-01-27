package com.dpl.dominlist.dplaudioplayer;

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
  }
}