package org.hfoss.posit;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.StringTokenizer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
/**
 *
 * @author pras
 *	Not yet workable.. as a place holder!
 */
public class PictureDB {

    public static final String KEY_ROWID="_id";
    public static final String KEY_DESCRIPTION="description";
    public static final String KEY_IMAGE="image";

	
    /**
    class Row extends Object {
        public String description;
        public String image;
        public long rowId;
        public String title;
    }
***/
    private static final String DATABASE_NAME = "posit5";
    private static final String DATABASE_TABLE = "pictures";
    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_CREATE =
        "create table " + DATABASE_TABLE +  "(_id integer primary key autoincrement, "
            + "description text, image text);";


    private SQLiteDatabase db;
    private final Context mCtx;

    public PictureDB(Context ctx) {
    	mCtx = ctx;
    }

    /**
     * Open the posit database. If it cannot be opened, try to create a new instance of
     * the database. If it cannot be created, throw an exception to signal the failure
     * @return this (self reference, allowing this to be chained in an initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public PictureDB open() throws SQLException {
  //  	mCtx.deleteDatabase(DATABASE_NAME);
        try {
            db = mCtx.openDatabase(DATABASE_NAME, null);
        } catch (FileNotFoundException e ) {
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

    public long createRow(String desc, String img) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_DESCRIPTION, desc);
        initialValues.put(KEY_IMAGE, img);

        return db.insert(DATABASE_TABLE, null, initialValues);
    }

    public void deleteRow(long rowId) {
        db.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null);
    }
    
    /* 
     * Returns the number of entries in the table
     * i.e, the total number of pictures
     */
    
    public int getCount()
    {
    	String s = ""+this.fetchAllRows().count();
    	Log.e("PictureDB",s);
    	return this.fetchAllRows().count();    	
    	
    }
    
    /**
     * Return a Cursor over the list of all rows in the database. 
     * Returning a cursor is more efficient than returning a list. See tutorial.
     * @return Cursor over all notes
     */
    public Cursor fetchAllRows() {
        return db.query(DATABASE_TABLE, new String[] {
                KEY_ROWID, KEY_DESCRIPTION}, 
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
                KEY_ROWID, KEY_DESCRIPTION, KEY_IMAGE}, 
                KEY_ROWID + "=" + rowId, null, null, null, null);
        if ((result.count() == 0) || !result.first()) {
            throw new SQLException("No note matching ID: " + rowId);
        }
        return result;
    }
    
    public Cursor fetchRows(long from) throws SQLException {
        Cursor result = db.query(true, DATABASE_TABLE, new String[] {
                KEY_ROWID, KEY_DESCRIPTION, KEY_IMAGE}, 
                KEY_ROWID + ">=" + from , null, null, null, null);
       // if ((result.count() == 0) || !result.first()) {
         //   throw new SQLException("No note matching ID: " + from);
       // }
        return result;
    }
    
    public Cursor fetchRows (String list){
    	String SQLQueryString = "";
    	StringTokenizer  x = new StringTokenizer(list,",");
    	while (x.hasMoreTokens()){
    		SQLQueryString += KEY_ROWID+"="+x.nextToken()+((x.hasMoreTokens())?" OR ":"");
    	}
    	Log.e("MyTAG", SQLQueryString);
    	Cursor result = db.query(true, DATABASE_TABLE, new String[] {
                KEY_ROWID, KEY_DESCRIPTION, KEY_IMAGE}, 
                SQLQueryString , null, null, null, null);
                
    	return result;
    }
    

    
    /**
     * Update the row using the details provided. The row to be updated is specified using
     * the rowId, and it is altered to use the title and desc values passed in
     * @param rowId id of note to update
     * @param title value to set note title to
     * @param body value to set note body to
     * @return true if the note was successfully updated, false otherwise
     */
    public boolean updateRow(long rowId, String desc, String img) {
        ContentValues args = new ContentValues();
        args.put(KEY_DESCRIPTION, desc);
        args.put(KEY_IMAGE, img);


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


