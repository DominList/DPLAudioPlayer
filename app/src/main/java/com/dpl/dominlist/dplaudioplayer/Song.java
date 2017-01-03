package com.dpl.dominlist.dplaudioplayer;

/**
 * Created by dominlist on 21.12.2016.
 */

public class Song implements Comparable<Song> {

    private String title;
    private String artist;
    private String album;
    private String songPath;
    private int indexOnPlayslist;

    public Song() {

    }

    public Song(String mArtist, String mTitle, String mAlbum, String mSongPath) {

        artist = mArtist;
        title = mTitle;
        album = mAlbum;
        songPath = mSongPath;
    }

    public int getIndexOnPlayslist() {
        return indexOnPlayslist;
    }

    public void setIndexOnPlayslist(int indexOnPlayslist) {
        this.indexOnPlayslist = indexOnPlayslist;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
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
    public int compareTo(Song song) {
        int compareArtist = artist.compareTo(song.artist);
        int compareTitle = title.compareTo(song.title);
        if (compareArtist == 0) {
            if (compareTitle == 0) {
                return album.compareTo(song.album);
            } else {
                return compareTitle;
            }
        } else
            return compareArtist;
    }
}
