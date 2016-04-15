package com.jgersztyn.pothole_pal;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/*
Data source class
 */
public class PinPointDataSrc {

    protected MySQLiteHelper dbHelper;
    protected SQLiteDatabase db;

    //this array stores the columns of the db which will needed in the getAllPoints() method
    String cols[] = {MySQLiteHelper.TEXT, MySQLiteHelper.POSITION};

    /*
    Constructor for this class
    @param context is the current representation of the database we need to respresent
     */
    public PinPointDataSrc(Context context) {
        dbHelper = new MySQLiteHelper(context);
    }

    /*
    Opens the database and allows it to be written to
     */
    public void open() {
        db = dbHelper.getWritableDatabase();
    }

    /*
    Closes the database connection after we no longer need it
     */
    public void close() {
        db.close();
    }

    /*
    Adds a marker into the database
    @param point the pinpoint to be added inside of the database
     */
    public void addMarker(PinPointObj point) {
        //an object to hold the values to be added into the database
        ContentValues values = new ContentValues();

        values.put(MySQLiteHelper.TEXT, point.getText());
        values.put(MySQLiteHelper.POSITION, point.getPosition());

        //insert the new data object into the database
        db.insert(MySQLiteHelper.TABLE_NAME, null, values);
    }

    /*
    Return a list of all points inside of the database
    @return all pinpoints that are currently stored inside of the database
     */
    public List<PinPointObj> getAllPoints() {
        List<PinPointObj> points = new ArrayList<PinPointObj>();

        //this represents a single row on the db table
        Cursor cursor = db.query(MySQLiteHelper.TABLE_NAME, cols, null, null, null, null, null);

        //ensure we are at the first cursor inside of the database
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            //the cursor must be converted to a unique point on the map
            PinPointObj singlePoint =  cursorToPoint(cursor);
            //add the point into the database
            points.add(singlePoint);
            //move to next cursor
            cursor.moveToNext();
        }
        cursor.close();

        return points;
    }

    /*
    Gets the information from a specified cursor and converts it into a point
    The point is then in a format that can be stored inside of the database
    @param cursor the cursor being passed in and converted into a point
     */
    public PinPointObj cursorToPoint(Cursor cursor) {
        PinPointObj point = new PinPointObj();
        //0 represents an array, or the column into which this value will be stored
        point.setText(cursor.getString(0));
        //this is the next column
        point.setPosition(cursor.getString(1));
        //any additional columns would need to be added somewhere here

        return  point;
    }

}
