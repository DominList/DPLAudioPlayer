package com.dpl.dominlist.dplaudioplayer;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by dominlist on 21.12.2016.
 */

public class SongAdapter extends ArrayAdapter<Song> {

    /**
     * New constructor
     *
     * @param context
     * @param objects
     */
    public SongAdapter(Context context, List objects) {
        super(context, 0, objects);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Song song = getItem(position);
        Tools util = new Tools();
        if (convertView == null) {
            convertView =
                    LayoutInflater.from(getContext()).inflate(R.layout.playlist_item, parent, false);
        }

        ImageView albumImageView = (ImageView) convertView.findViewById(R.id.album_image);
        TextView artistTextView = (TextView) convertView.findViewById(R.id.song_artist);
        TextView titleTextView = (TextView) convertView.findViewById(R.id.song_title);
        TextView durationTextView = (TextView) convertView.findViewById(R.id.duration);

        albumImageView.setImageResource(R.drawable.ic_image_note);
        artistTextView.setText(song.getArtist());
        titleTextView.setText(song.getTitle());
        durationTextView.setText(util.milliSecondsToTimer(song.getDuration()));



        return convertView;
    }
}
