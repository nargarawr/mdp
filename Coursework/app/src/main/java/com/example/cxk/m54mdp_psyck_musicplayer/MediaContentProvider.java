package com.example.cxk.m54mdp_psyck_musicplayer;

import android.app.Activity;
import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.database.Cursor;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.util.Log;
import android.widget.TextView;


public class MediaContentProvider extends ListActivity {

    // Request code for when deeper levels of the MediaContentProvider finish
    static final int ALBUM_REQUEST_CODE = 1;
    static final int SONG_REQUEST_CODE = 2;

    // Media store variables for easy access
    final static String ID = MediaStore.Audio.Media._ID;
    final static String TRACK = MediaStore.Audio.Media.TRACK;
    final static String TITLE = MediaStore.Audio.Media.TITLE;
    final static String ARTIST = MediaStore.Audio.Media.ARTIST;
    final static String DURATION = MediaStore.Audio.Media.DURATION;
    final static String ALBUM = MediaStore.Audio.Media.ALBUM;
    final static String YEAR = MediaStore.Audio.Media.YEAR;
    Uri content_uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

    // Used to tell the MediaContentProvider what type of content to retrieve
    final static String SONG = "SONG";
    final static String TYPE = "TYPE";
    private String media_type;

    // Used internally to filter the results of the queries
    private String artist_filter;
    private String album_filter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_content_provider);
        Log.d("myapp", "MediaContentProvider onCreate");

        Bundle bundle = getIntent().getExtras();
        media_type = bundle.getString(TYPE);
        artist_filter = bundle.getString(ARTIST, null);
        album_filter = bundle.getString(ALBUM, null);

        getContent();
    }

    /**
     * Gets and displays the content for this media content provider. This will be, depending on
     * the value of the {media_type} variable: ARTIST, all artists on the phone; ALBUM, all albums
     * for a given artist on the phone; or SONG, all songs for a given album (and artist) on the phone.
     */
    public void getContent() {
        String colsToSelect[];
        String colsToDisplay[];
        int colsToDisplayLocation[];
        int layout;
        String whereClause = null;

        if (!(media_type.equals(SONG))) {
            // If we're getting albums or artists, we only want that specific field
            colsToSelect = new String[]{ID, media_type};
            colsToDisplay = new String[]{media_type};
            colsToDisplayLocation = new int[]{R.id.main_display};
            layout = R.layout.generic_media_list_entry;
        } else {
            // If we're getting songs, we want to get a little more information
            colsToSelect = new String[]{ID, TRACK, TITLE, DURATION};
            colsToDisplay = new String[]{TRACK, TITLE, DURATION};
            colsToDisplayLocation = new int[]{R.id.song_number, R.id.main_display, R.id.song_duration};
            layout = R.layout.song_media_list_entry;
        }

        // Here we define the where clause for our query. If we're at the artist selection stage
        // we only need to do the group_by hack to make sure there are no duplicates. If we're on
        // the album selection stage, we need to only display albums for the artist we selected
        // in the artist stage, and if we're on the song selection stage, we should only display
        // the songs for the given artist and album.
        if (media_type.equals(ARTIST)) {
            whereClause = " 1=1 ) GROUP BY ( " + ARTIST;
        } else if (media_type.equals(ALBUM)) {
            whereClause = ARTIST + " = '" + artist_filter + "'" +
                    " AND 1=1 ) GROUP BY ( " + ARTIST;
        } else if (media_type.equals(SONG)) {
            whereClause = ARTIST + " = '" + artist_filter + "'" +
                " AND " + ALBUM + " = '" + album_filter + "'";
        }

        // Actually get the content now
        ContentResolver cr = getContentResolver();
        Cursor cursor = cr.query(content_uri, colsToSelect, whereClause, null, null);

        // Display the content in the correct view
        SimpleCursorAdapter sca = new SimpleCursorAdapter(
                this,
                layout,
                cursor,
                colsToDisplay,
                colsToDisplayLocation
        );
        this.setListAdapter(sca);
    }

    /**
     * When we click on one of the list items, we want to then go deeper into that element.
     * If clicking an artist, it will show their albums, and when clicking an album, it will
     * show all songs in that album.
     */
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        // Get the text of the list item we clicked on
        TextView textView = (TextView) v.findViewById(R.id.main_display);
        String selectedValue = textView.getText().toString();

        // If we're clicking on a song name, we should get the song name and send this back
        Bundle bundle = new Bundle();
        if (media_type.equals(SONG)) {
            bundle.putString(ALBUM, album_filter);
            bundle.putString(ARTIST, artist_filter);
            bundle.putString(SONG, selectedValue);

            Intent result = new Intent();
            result.putExtras(bundle);
            setResult(Activity.RESULT_OK, result);
            finish();
            return;
        }

        // If we selected an album or artist, we spawn another MediaContentProvider to search one level deeper
        Intent intent = new Intent(MediaContentProvider.this, MediaContentProvider.class);
        int requestCode;
        if (media_type.equals(ARTIST)) {
            bundle.putString(TYPE, ALBUM);
            bundle.putString(ARTIST, selectedValue);
            requestCode = ALBUM_REQUEST_CODE;
        } else { // equals(ALBUM)
            bundle.putString(TYPE, SONG);
            bundle.putString(ARTIST, artist_filter);
            bundle.putString(ALBUM, selectedValue);

            requestCode = SONG_REQUEST_CODE;
        }

        intent.putExtras(bundle);
        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ALBUM_REQUEST_CODE && resultCode == RESULT_OK) {
            setResult(Activity.RESULT_OK, data);
            finish();
        } else if (requestCode == SONG_REQUEST_CODE && resultCode == RESULT_OK) {
            setResult(Activity.RESULT_OK, data);
            finish();
        }
    }
}
