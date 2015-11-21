package com.example.cxk.mupsyck;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Class DBHelper
 * <p/>
 * Used to facilitate access to the phone's internal database.
 * Currently only used to save and retrieve song ratings
 */
public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "auxMusicDatabase";
    private SQLiteDatabase db;

    /**
     * Default Constructor, assigns member variables
     *
     * @param context Application context
     */
    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
        this.db = this.getWritableDatabase();
    }

    /**
     * Creates all the tables in the database when the database is created
     *
     * @param db The database
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE tb_music_rating (" +
                        "pk_music_rating_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "song_path VARCHAR(256) UNIQUE ON CONFLICT REPLACE," +
                        "song_rating VARCHAR(1)" +
                        ");"
        );
    }

    /**
     * When the database is upgraded, it is recreated from scratch
     *
     * @param db         The database
     * @param oldVersion The old version of the database
     * @param newVersion The new version of the database
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS tb_music_rating");
        onCreate(db);
    }

    /**
     * Updates the rating for a given song in the database. Inserts a row if one does not exist,
     * and replaces the value of the row if it does
     *
     * @param songPath   The path of the song (unique identifier)
     * @param songRating The rating to be given
     */
    public void updateSongRating(String songPath, int songRating) {
        db.execSQL(
                "INSERT INTO tb_music_rating (" +
                        "song_path, song_rating" +
                        ") VALUES (" +
                        "'" + songPath + "', " + songRating +
                        ");"
        );
    }

    /**
     * Gets the rating of a particular song
     *
     * @param path The path of the song (unique identifier) to search for
     * @return The rating this song received, returns 0 if there are no results
     */
    public String getSongRating(String path) {
        // Get the song out of the database with the corresponding path
        Cursor c = db.query(
                "tb_music_rating",
                new String[]{"pk_music_rating_id", "song_path", "song_rating"},
                "song_path = ?",
                new String[]{path},
                null,
                null,
                null
        );

        // This query should only ever return one return, so we get the information from the first result
        String songRating = "0";
        if (c.moveToFirst()) {
            songRating = c.getString(2);
        }

        c.close();
        return songRating;
    }
}
