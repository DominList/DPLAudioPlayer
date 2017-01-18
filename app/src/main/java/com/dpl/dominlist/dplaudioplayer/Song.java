package com.dpl.dominlist.dplaudioplayer;

/**
 * Created by dominlist on 21.12.2016.
 */

public class Song implements Comparable<Song> {

    private String title;
    private String artist;
    private String album;
    private String songPath;
    private int duration;
    private long id;

    public Song() {

        artist = "Unknown artist";
        title = "Unknown title";
        album = "Unknown album";

    }

    public Song(long songId, String mArtist, String mTitle, String mAlbum, String mSongPath) {

        artist = mArtist.isEmpty() ? "Unknown artist" : mArtist;
        title = mTitle.isEmpty() ? "Unknown title" : mTitle;
        album = mAlbum.isEmpty() ? "Unknown album" : mAlbum;
        id = songId;
        songPath = mSongPath;
    }


    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return this.artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return this.album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }


    public String getSongPath() {
        return songPath;
    }

    public void setSongPath(String songPath) {
        this.songPath = songPath;
    }

    @Override
    public String toString(){
        return "Song: id:" + id+ "-" +artist+"-"+album+"-"+title;
    }


    @Override
    public int compareTo(Song song) {
        int compareArtist = artist.isEmpty() && song.getArtist().isEmpty() ? 0 : artist.compareTo(song.getArtist());
        int compareTitle = title.isEmpty() && song.getTitle().isEmpty() ? 0 : title.compareTo(song.getTitle());
        if ( compareArtist == 0) {
            if ( compareTitle == 0) {
                return album.compareTo(song.getAlbum());
            } else {
                return compareTitle;
            }
        } else
            return compareArtist;
    }
}
