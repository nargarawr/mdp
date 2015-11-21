package com.example.cxk.mupsyck;


import android.Manifest;
import android.app.Activity;
import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.database.Cursor;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Class MediaContentProvider
 * <p/>
 * Used to retrieve and display artists, albums and songs on the phone.
 * It is called from the PlayerActivity, with a {media_type} of ARTIST. Which will display all distinct artists
 * on the user's phone. Clicking on one of these artists will cause this class to call itself, but with
 * a {media_type} of ALBUM. This will then list all distinct albums for this artist. Clicking on one
 * of the albums will cause the class to call itself for a second time (now three copies will be running)
 * and display all songs for the provided artist and album combination. Clicking on one of these songs
 * will pass back the song name, album name, and artist name of the song that was selected. From this
 * information, the PlayerActivity will call another query which will get the whole album, and start
 * playing the artist from the selected track.
 */
public class MediaContentProvider extends ListActivity {

    // Request code for when deeper levels of the MediaContentProvider finish
    static final int ALBUM_REQUEST_CODE = 1;
    static final int SONG_REQUEST_CODE = 2;

    // Request code for when we ask for the READ_EXTERNAL_STORAGE permission
    static final int PERMISSION_REQUEST_CODE = 3;

    // Used for broadcasts from this activity
    static final String DISPLAY_PLAYLIST = "DISPLAY_PLAYLIST";

    // Used for passing the Intent out of the class back to MainActivity
    static final String SONG_ARRAY = "SONG_ARRAY";
    static final String START_FROM = "START_FROM";
    static final String ALBUM_ART_PATH = "ALBUM_ART_PATH";

    // Used to tell the PlayerActivity that there is no music on the phone
    final static int NO_MUSIC = 10;

    // Media store variables for easy access
    static final String ID = MediaStore.Audio.Media._ID;
    static final String TRACK = MediaStore.Audio.Media.TRACK;
    static final String TITLE = MediaStore.Audio.Media.TITLE;
    static final String ARTIST = MediaStore.Audio.Media.ARTIST;
    static final String DURATION = MediaStore.Audio.Media.DURATION;
    static final String ALBUM = MediaStore.Audio.Media.ALBUM;
    static final String ARTWORK = MediaStore.Audio.Albums.ALBUM_ART;
    static final String ALBUM_ID = MediaStore.Audio.Media.ALBUM_ID;
    static final String FILEPATH = MediaStore.Audio.Media.DATA;
    private Uri content_uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

    // Used to tell the MediaContentProvider what type of content to retrieve
    static final String SONG = "SONG";
    static final String TYPE = "TYPE";
    private String media_type;

    // Used internally to filter the results of the queries
    private String artist_filter;
    private String album_filter;

    /**
     * Called when the content provider is created and determines which type of results it should
     * display. If we're loading artists, make sure we have permission to, and if we're loading
     * songs, add listeners to the list view so we can convert the duration of the songs to a nicer format
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_content_provider);

        Bundle bundle = getIntent().getExtras();
        media_type = bundle.getString(TYPE);
        artist_filter = bundle.getString(ARTIST, null);
        album_filter = bundle.getString(ALBUM, null);

        TextView listTitle = (TextView) findViewById(R.id.listTitle);
        if (media_type.equals(ALBUM)) {
            listTitle.setText(artist_filter);
        } else if (media_type.equals(SONG)) {
            listTitle.setText(album_filter);

            // When we create the list view, we call the formatSongDuration function, which will
            // loop through each element and update the duration to an MM:SS format, instead of
            // just being in milliseconds. We also do this whenever we scroll
            final ListView list = (ListView) findViewById(android.R.id.list);
            list.post(new Runnable() {
                @Override
                public void run() {
                    formatSongDuration(list);
                }
            });

            list.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    /*
                     * The reason that this is included, but commented out, is because there is a bug
                     * in Android that causes this method to spam the console with tons of
                     * "requestLayout() improperly called by android.widget.TextView" errors - or maybe it's just my bad code
                     *
                     * If this line was to be included, the duration of the songs would be formatted
                     * as the user is scrolling, which would be a lot nicer than on scroll finished,
                     * like it is at the moment
                     */
                    //formatSongDuration(list);
                }

                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                    formatSongDuration(list);
                }
            });
        }

        // If the SD Card is not connected, close the activity
        String state = Environment.getExternalStorageState();
        if (!(Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state))) {
            setResult(NO_MUSIC, null);
            finish();
        }

        // As of Android 6.0, with a target SDK of 23 of higher, you have to specifically request
        // a dangerous permission if you're going to use it. Because my phone fits these requirements,
        // I have added these permission checks so I can test my application both on the emulator,
        // and on my real phone
        int readPermission = ContextCompat.checkSelfPermission(
                MediaContentProvider.this,
                Manifest.permission.READ_EXTERNAL_STORAGE
        );
        // If we don't have the permission, ask for it, otherwise read from the sd card
        if (media_type.equals(ARTIST) && readPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    MediaContentProvider.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE
            );
        } else {
            getContent();
        }
    }

    /**
     * After asking for permission to access the external storage, we call this method to continue
     * processing. If the permission is allowed, we get the content from the SD card and continue.
     * Otherwise we return to the PlayerActivity and do nothing
     *
     * @param requestCode  The request code of this permission
     * @param permissions  The permissions asked for
     * @param grantResults The permissions granted
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContent();
                } else {
                    // If permissions are denied, close the window as if nothing happened
                    finish();
                }
            }
        }
    }

    /**
     * Gets and displays the content for this media content provider. This will be, depending on
     * the value of the {media_type} variable: ARTIST, all artists on the phone; ALBUM, all albums
     * for a given artist on the phone; or SONG, all songs for a given album (and artist) on the phone.
     * <p/>
     * <b> NB. The emulator doesn't seem to update when new music is added to the phone,
     * but running it on an actual phone will (i.e, if you add new music it will be pulled in. </b>
     */
    public void getContent() {
        String colsToSelect[];
        String colsToDisplay[];
        int colsToDisplayLocation[];
        int layout;
        String whereClause = null;
        String sortBy = null;

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
            sortBy = TRACK + " ASC";
        }

        // Here we define the where clause for our query. If we're at the artist selection stage we
        // only need to do the group_by hack to make sure there are no duplicates. If we're on the album
        // selection stage, we need to only display albums for the artist we selected in the artist stage,
        // and if we're on the song selection stage, we should only display the songs for the given artist
        // and album.
        switch (media_type) {
            case ARTIST:
                whereClause = " 1=1 ) GROUP BY ( " + ARTIST;
                break;
            case ALBUM:
                whereClause = ARTIST + " = '" + artist_filter + "'" +
                        " AND 1=1 ) GROUP BY ( " + ARTIST;
                break;
            case SONG:
                whereClause = ARTIST + " = '" + artist_filter + "'" +
                        " AND " + ALBUM + " = '" + album_filter + "'";
                break;
        }

        // Actually get the content now
        ContentResolver cr = getContentResolver();
        Cursor cursor = cr.query(content_uri, colsToSelect, whereClause, null, sortBy);

        // If there is no music to be found, return and display error toast
        if (media_type.equals(ARTIST) && cursor.getCount() == 0) {
            setResult(NO_MUSIC, null);
            finish();
        }

        // If there is music, display it in a list view
        if (cursor != null && cursor.getCount() > 0) {
            SimpleCursorAdapter sca = new SimpleCursorAdapter(
                    this,
                    layout,
                    cursor,
                    colsToDisplay,
                    colsToDisplayLocation
            );
            this.setListAdapter(sca);
        }
    }

    /**
     * When we click on one of the list items, we want to then go deeper into that element.
     * If clicking an artist, it will show their albums, and when clicking an album, it will
     * show all songs in that album.
     *
     * @param l        The ListView itself
     * @param v        The view
     * @param position The position this element has in the list view
     * @param id       The ID of this element
     */
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        // Get the text of the list item we clicked on
        TextView textView = (TextView) v.findViewById(R.id.main_display);
        String selectedValue = textView.getText().toString();

        // If we're clicking on a song name, we should get the song and send this back
        if (media_type.equals(SONG)) {
            getAllSongsForAlbumAndReturn(
                    artist_filter,
                    album_filter,
                    selectedValue
            );
            return;
        }

        // If we selected an album or artist, we spawn another MediaContentProvider to search one level deeper
        Intent intent = new Intent(MediaContentProvider.this, MediaContentProvider.class);
        Bundle bundle = new Bundle();
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

    /**
     * Deals with the returning of the nested MediaContentProviders. If we return from any
     * MediaContentProvider with the back button, we do nothing. If we have selected a song
     * on the song selection screen, we pass the selected song back to the first instance of
     * the MediaContentProvider stack, where we then get all songs for that album
     *
     * @param requestCode The request code given to this Activity
     * @param resultCode  The result code from the spawned Activity
     * @param data        Any data being returned
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((requestCode == ALBUM_REQUEST_CODE || requestCode == SONG_REQUEST_CODE) && resultCode == RESULT_OK) {
            setResult(Activity.RESULT_OK, data);
            finish();
        }
    }

    /**
     * Get album artwork for an album
     *
     * @param albumId An album id for the album to get the artwork for
     * @return The path to the album artwork for the provided album
     */
    private String getAlbumArtwork(String albumId) {
        String colsToSelect[] = {MediaStore.Audio.Albums._ID, ARTWORK};
        String whereClause = MediaStore.Audio.Albums._ID + " = " + albumId;

        ContentResolver cr = getContentResolver();
        Cursor cursor = cr.query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, colsToSelect, whereClause, null, null);

        String albumArtwork = "";
        if (cursor.moveToFirst()) {
            do {
                albumArtwork = cursor.getString(cursor.getColumnIndex(ARTWORK));
            } while (cursor.moveToNext());
        }
        cursor.close();


        Log.d("cxk-db", "Album artwork - " + albumArtwork);

        return albumArtwork;
    }

    /**
     * TODO
     * @return
     */
    private String getSongRating(Song s) {
        DBHelper dbHelper = new DBHelper(this);
        //SQLiteDatabase database = dbHelper.getWritableDatabase();
        return dbHelper.getSongRating(s.getFilepath());
    }

    /**
     * Gets all songs for the given artist and album, and then returns this and ends the activity.
     * Also passes back a {start_from} parameter, which is the position of the song that we should
     * start playback from (determined by the value of {songName}).
     *
     * @param artist   The artist to playback from
     * @param album    The album to playback from
     * @param songName The song to start playback from
     */
    private void getAllSongsForAlbumAndReturn(String artist, String album, String songName) {
        String colsToSelect[] = {ID, TRACK, TITLE, ARTIST, DURATION, ALBUM, FILEPATH, ALBUM_ID};
        String whereClause = ARTIST + " = '" + artist + "' AND " + ALBUM + " = '" + album + "'";
        String sortBy = TRACK + " ASC";

        ContentResolver cr = getContentResolver();
        Cursor cursor = cr.query(content_uri, colsToSelect, whereClause, null, sortBy);

        Intent result = new Intent();
        Bundle bundle = new Bundle();
        ArrayList<Song> songs = new ArrayList<>();
        int start_from = 0;
        int index = 0;
        String albumId = "";

        if (cursor.moveToFirst()) {
            do {
                songs.add(new Song(
                        cursor.getString(cursor.getColumnIndex(ARTIST)),
                        cursor.getString(cursor.getColumnIndex(ALBUM)),
                        cursor.getString(cursor.getColumnIndex(TITLE)),
                        cursor.getString(cursor.getColumnIndex(DURATION)),
                        cursor.getString(cursor.getColumnIndex(TRACK)),
                        cursor.getString(cursor.getColumnIndex(FILEPATH))
                ));

                albumId = cursor.getString(cursor.getColumnIndex(ALBUM_ID));

                if (cursor.getString(cursor.getColumnIndex(TITLE)).equals(songName)) {
                    start_from = index;
                }

                index++;
            } while (cursor.moveToNext());
        }
        cursor.close();

        String path = getAlbumArtwork(albumId);

        // Set the artwork and rating for each of the songs
        for (Song s : songs) {
            s.setArtwork(path);
            s.setRating(getSongRating(s));
        }

        bundle.putParcelableArrayList(SONG_ARRAY, songs);
        bundle.putInt(START_FROM, start_from);
        bundle.putString(ALBUM_ART_PATH, path);

        result.putExtras(bundle);

        setResult(Activity.RESULT_OK, result);
        finish();
    }

    /**
     * Used as the onScroll listener for the ListView. Will format each of the visible song durations
     * on the song display screen into MM:SS format, rather than milliseconds
     *
     * @param list The list view to format
     */
    public void formatSongDuration(ListView list) {
        int childCount = list.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View v = list.getChildAt(i);
            TextView songDuration = (TextView) v.findViewById(R.id.song_duration);
            songDuration.setText(getFormattedTime(songDuration.getText().toString()));
        }
    }

    /**
     * Converts a song's duration from milliseconds to HH:MM:SS
     *
     * @param timeInMs The duration of a song in milliseconds
     * @return The formatted time
     */
    public String getFormattedTime(String timeInMs) {
        // Try to convert the ms into a long. If there is an exception, it has already been formatted
        long ms;
        try {
            ms = Long.parseLong(timeInMs);
        } catch (Exception e) {
            return timeInMs;
        }

        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(ms),
                TimeUnit.MILLISECONDS.toSeconds(ms) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(ms))
        );
    }
}
