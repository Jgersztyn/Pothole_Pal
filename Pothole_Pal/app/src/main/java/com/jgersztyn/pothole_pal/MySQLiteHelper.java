package com.jgersztyn.pothole_pal;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/*
This class is required to create a database for out application
 */
public class MySQLiteHelper extends SQLiteOpenHelper {

    //the name of the database table
    public static final String TABLE_NAME = "points";

    //unique id to represent each database item
    public static final String COLUMN_ID = "point_id";

    //database fields
    public static final String TEXT = "point_title";
    //public static final String SNIPPET = "point_snippet";
    public static final String POSITION = "point_position";

    //a file name used to describe our database
    private static final String DATABASE_NAME = "markerlocations.db";

    //specifies the version of this database
    private static final int DATABASE_VERSION = 1;

    //database creation sql statement
    private static final String DATABASE_CREATE = "create table "
            + TABLE_NAME + "(" + COLUMN_ID
            + " integer primary key autoincrement, "
            + TEXT + " text, "
            //+ SNIPPET + " text, "
            + POSITION + " text not null);"; //position must exist to display a point

    /*
    Constructor for the database
    @param context the database context to be set up
     */
    public MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(MySQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

}

//import android.support.v7.app.AppCompatActivity;
//import android.os.Bundle;
//
//public class MySQLiteHelper extends AppCompatActivity {
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_my_sqlite_helper);
//    }
//}
