package com.example.cxk.mupsyck;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * TODO
 */
public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "auxMusicDatabase";

    /**
     * TODO
     * @param context
     */
    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    /**
     * TODO
     * @param db
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE tb_music_rating (" +
                        "   pk_music_rating_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "   song_path VARCHAR(256) UNIQUE," +
                        "   song_rating VARCHAR(1)" +
                ");"
        );
    }

    /**
     * TODO
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS tb_music_rating");
        onCreate(db);
    }

    /**
     * TODO
     * @param songRating
     */
    public void updateSongRating(String songRating) {

    }

    /**
     * TODO
     * @param path
     * @return
     */
    public String getSongRating(String path) {
        return "4";
    }
}
