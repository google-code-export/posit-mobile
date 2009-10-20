package org.hfoss.posit;

import java.io.FileNotFoundException;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
/**
 *
 * @author pras
 *	Not yet workable.. as a place holder!
 */
public class DBHelper {

    public static final String KEY_ROWID="_id";
    public static final String KEY_DESCRIPTION="description";
    public static final String KEY_IMAGE="image";
    public static final String KEY_LONGITUDE="longitude";
    public static final String KEY_LATITUDE="latitude";
	
    /**
    class Row extends Object {
        public String description;
        public String image;
        public long rowId;
        public String title;
    }
***/
    private static final String DATABASE_NAME = "posit4";
    private static final String DATABASE_TABLE = "finds";
    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_CREATE =
        "create table " + DATABASE_TABLE +  "(_id integer primary key autoincrement, "
            + "description text, image text, longitude text, latitude text);";


    private SQLiteDatabase db;
    private final Context mCtx;

    public DBHelper(Context ctx) {
    	mCtx = ctx;
    }

    /**
     * Open the posit database. If it cannot be opened, try to create a new instance of
     * the database. If it cannot be created, throw an exception to signal the failure
     * @return this (self reference, allowing this to be chained in an initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public DBHelper open() throws SQLException {
  //  	mCtx.deleteDatabase(DATABASE_NAME);
        try {
            db = mCtx.openDatabase(DATABASE_NAME, null);
        } catch (FileNotFoundException e) {
            try {
                db =
                    mCtx.createDatabase(DATABASE_NAME, DATABASE_VERSION, 0, null);
                db.execSQL(DATABASE_CREATE);
            } catch (FileNotFoundException e1) {
                throw new SQLException("Could not create database");
            }
        }
        return this;
    }
    
    public void close() {
        db.close();
    }

    public long createRow(String desc, String img, String longitude, String latitude) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_DESCRIPTION, desc);
        initialValues.put(KEY_IMAGE, img);
        initialValues.put(KEY_LONGITUDE, longitude);
        initialValues.put(KEY_LATITUDE, latitude);
        return db.insert(DATABASE_TABLE, null, initialValues);
    }

    public void deleteRow(long rowId) {
        db.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null);
    }

    /**
     * Return a Cursor over the list of all rows in the database. 
     * Returning a cursor is more efficient than returning a list. See tutorial.
     * @return Cursor over all notes
     */
    public Cursor fetchAllRows() {
        return db.query(DATABASE_TABLE, new String[] {
                KEY_ROWID, KEY_DESCRIPTION, KEY_IMAGE, KEY_LONGITUDE, KEY_LATITUDE}, 
                null, null, null, null, null);
    }
    
    /****
    public List<Row> fetchAllRows() {
        ArrayList<Row> ret = new ArrayList<Row>();
        try {
            Cursor c =
                db.query(DATABASE_TABLE, new String[] {
                    "rowid", "title", "description","image"}, null, null, null, null, null);
            int numRows = c.count();
            c.first();
            for (int i = 0; i < numRows; ++i) {
                Row row = new Row();
                row.rowId = c.getLong(0);
                row.title = c.getString(1);
                row.description = c.getString(2);
                row.image = c.getString(3);
                ret.add(row);
                c.next();
            }
        } catch (SQLException e) {
            Log.e("MyLog", e.toString());
        }
        return ret;
    }
    ***/

    /**
     * Return a Cursor positioned at the note that matches the given rowId
     * @param rowId id of note to retrieve
     * @return Cursor positioned to matching note, if found
     * @throws SQLException if note could not be found/retrieved
     */
    public Cursor fetchRow(long rowId) throws SQLException {
        Cursor result = db.query(true, DATABASE_TABLE, new String[] {
                KEY_ROWID, KEY_DESCRIPTION, KEY_IMAGE, KEY_LONGITUDE, KEY_LATITUDE}, 
                KEY_ROWID + "=" + rowId, null, null, null, null);
        if ((result.count() == 0) || !result.first()) {
            throw new SQLException("No note matching ID: " + rowId);
        }
        return result;
    }
    /***
    public Row fetchRow(long rowId) {
        Row row = new Row();
        Cursor c =
            db.query(true, DATABASE_TABLE, new String[] {
                "rowid", "title", "description", "image"}, "rowid=" + rowId, null, null,
                null, null);
        if (c.count() > 0) {
            c.first();
            row.rowId = c.getLong(0);
            row.title = c.getString(1);
            row.description = c.getString(2);
            row.image = c.getString(3);
            return row;
        } else {
            row.rowId = -1;
            row.description = row.title = row.image= null;
        }
        return row;
    }
***/
    
    /**
     * Update the row using the details provided. The row to be updated is specified using
     * the rowId, and it is altered to use the title and desc values passed in
     * @param rowId id of note to update
     * @param title value to set note title to
     * @param body value to set note body to
     * @return true if the note was successfully updated, false otherwise
     */
    public boolean updateRow(long rowId, String desc, String img, String lon, String lat) {
        ContentValues args = new ContentValues();
        args.put(KEY_DESCRIPTION, desc);
        args.put(KEY_IMAGE, img);
        args.put(KEY_LONGITUDE, lon);
        args.put(KEY_LATITUDE, lat);

        return db.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
    
    /****
    public void updateRow(long rowId, String title, String description, String image) {
        ContentValues args = new ContentValues();
        args.put("title", title);
        args.put("body", description);
        args.put("image", image);
        db.update(DATABASE_TABLE, args, "rowid=" + rowId, null);
    }
    ***/
}


