package org.hfoss.posit;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Iterator;

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
public class DBHelper {

	/**
	 * Refactoring Notes: Add all this into a hashMap kind of thing,load on the fly.
	 */

    public static final String KEY_ROWID="_id";
    public static final String KEY_DESCRIPTION="description";
    public static final String KEY_TYPE="type";
    public static final String KEY_IDENTIFIER ="identifier";
    public static final String KEY_NAME="name";
    public static final String KEY_TIME="time";
    public static final String KEY_TAGGED="tagged";
    public static final String KEY_AGE = "age";
    public static final String KEY_SEX = "sex";
    public static final String KEY_LONGITUDE="longitude";
    public static final String KEY_LATITUDE="latitude";
	public static final String KEY_OBSERVED="observed";
	public static final String KEY_RECORD_ID="recordid";
	public static final String KEY_FILENAME="filename";
	public static final String KEY_WEATHER="weather";
    private static final String NAME = "posit-sci";
    private static final String MAIN_TABLE = "finds";
    private static final String IMAGES_TABLE = "images";
    private static final String SIGHTINGS_TABLE = "sightings";
    
    private static final int VERSION = 1;
    
    private static final String[] PROJECTION = new String[] {
    	KEY_ROWID,             /* Refactoring notes: somehow unify these things? */
    	KEY_DESCRIPTION,
    	//KEY_TYPE,
    	KEY_IDENTIFIER,
    	KEY_NAME,
    	KEY_TIME,
    	KEY_TAGGED,
    	KEY_AGE,
    	KEY_SEX,
    	KEY_LONGITUDE,
    	KEY_LATITUDE
    };
    
    //TODO recordid wont be integer forever, need to change it to text
    /*
     * Refactoring Notes: needs to be on a loop based on the values 
     * eg, create tuples from the hashmaps into a loop and loop through them to create
     * this script
     */
    //TODO remove latitude and longitude and perhaps replace them with a default session Id
    private static final String TABLE_MAIN_CREATE =
        "create table " + MAIN_TABLE +  "(_id integer primary key autoincrement, "+
        "name text, age integer, sex integer, identifier text, tagged integer, time text,"
        + "description text,  longitude double, latitude double );";

    
    private static final String TABLE_IMAGES_CREATE=
    	" create table "+IMAGES_TABLE + "(_id integer primary key autoincrement, filename text, " +
    			"recordid integer, description text);";

    private static final String TABLE_SIGHTINGS_CREATE=
    	"create table "+ SIGHTINGS_TABLE+"(_id integer primary key autoincrement,  name text, latitude double, longitude double, "+
    		"observed text, "/*for observation type*/+"description text, recordid integer, time text, weather text);";
    
    private SQLiteDatabase db;
    private final Context mCtx;
    private final String APP = "DBHelper";
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
  //  	mCtx.deleteDatabase(NAME);
        try {
            db = mCtx.openDatabase(NAME, null);
        } catch (FileNotFoundException e) {
            try {
                db =
                    mCtx.createDatabase(NAME, VERSION, 0, null);
                db.execSQL(TABLE_MAIN_CREATE);
                db.execSQL(TABLE_IMAGES_CREATE);
                db.execSQL(TABLE_SIGHTINGS_CREATE);
            } catch (FileNotFoundException e1) {
                throw new SQLException("Could not create database");
            }
        }
        return this;
    }
    
    public void close() {
        db.close();
    }
    /**
     * Create a new Find
     * @param desc
     * @param longitude
     * @param latitude
     * @param time
     * @param sex
     * @param age
     * @param identifier
     * @param name
     * @param tagged
     * @return
     */
    public long createRow(String desc, String longitude, String latitude, 
    		String time, int sex,int age, String identifier,String name, int tagged ){
    	ContentValues args = new ContentValues();
    	/*desc = "hello";
    	name="bird1";
    	age=10;
    	sex =1;
    	tagged=1;
    	identifier="xdfe3w";*/
    	
        args.put(KEY_DESCRIPTION, desc);
        // args.put(KEY_TYPE, type);
        args.put(KEY_NAME, name);
        args.put(KEY_AGE, age);
        args.put(KEY_SEX, sex);
        args.put(KEY_TIME, time);
        args.put(KEY_TAGGED, tagged);
        args.put(KEY_IDENTIFIER, identifier);
        args.put(KEY_LONGITUDE, Double.parseDouble(longitude));
        args.put(KEY_LATITUDE, Double.parseDouble(latitude));
        
        
        return db.insert(MAIN_TABLE, null, args);
    }
    
    public long createRow(String desc,  String longitude, String latitude, String type) {
        ContentValues args = new ContentValues();
        args.put(KEY_DESCRIPTION, desc);
        args.put(KEY_LONGITUDE, Double.parseDouble(longitude));
        args.put(KEY_LATITUDE, Double.parseDouble(latitude));
        args.put(KEY_TYPE, type);
        
        return db.insert(MAIN_TABLE, null, args);
    }

    public void deleteRow(long rowId) {
        db.delete(MAIN_TABLE, KEY_ROWID + "=" + rowId, null);
    }

    /**
     * Return a Cursor over the list of all rows in the database. 
     * Returning a cursor is more efficient than returning a list. See tutorial.
     * @return Cursor over all notes
     */
    public Cursor fetchAllRows() {
        return db.query(MAIN_TABLE, PROJECTION, 
                null, null, null, null, null);
    }
    public Cursor fetchSelectedColumns(String[] projection){
    	return db.query(MAIN_TABLE, projection,null,null,null, null, null);
    }
    
 

    /**
     * Return a Cursor positioned at the note that matches the given rowId
     * @param rowId id of note to retrieve
     * @return Cursor positioned to matching note, if found
     * @throws SQLException if note could not be found/retrieved
     */
    public Cursor fetchRow(long rowId) throws SQLException {
        Cursor result = db.query(true, MAIN_TABLE, PROJECTION, 
                KEY_ROWID + "=" + rowId, null, null, null, null);
        if ((result.count() == 0) || !result.first()) {
            throw new SQLException("No note matching ID: " + rowId);
        }
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
    public boolean updateRow(long rowId, String desc,  String lon, String lat, String type) {
        ContentValues args = new ContentValues();
        args.put(KEY_DESCRIPTION, desc);
        args.put(KEY_LONGITUDE, Double.parseDouble(lon));
        args.put(KEY_LATITUDE, Double.parseDouble(lat));
        args.put(KEY_TYPE, type);
        return db.update(MAIN_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
    /**
     * Update a find
     * @param rowId
     * @param desc
     * @param lon
     * @param lat
     * @param time
     * @param sex
     * @param age
     * @param identifier
     * @param name
     * @param tagged
     * @return
     */
    public boolean updateRow(long rowId, String desc,  String lon, String lat, 
    		String time, int sex,int age, String identifier,String name, int tagged ) {
        ContentValues args = new ContentValues();
        args.put(KEY_DESCRIPTION, desc);
        args.put(KEY_LONGITUDE, Double.parseDouble(lon));
        args.put(KEY_LATITUDE, Double.parseDouble(lat));
        //args.put(KEY_TYPE, type);
        args.put(KEY_TIME, time);
        args.put(KEY_SEX, sex);
        args.put(KEY_IDENTIFIER, identifier);
        args.put(KEY_AGE, age);
        args.put(KEY_NAME, name);
        args.put(KEY_TAGGED, tagged);
        
        return db.update(MAIN_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
    /**
     * Adds image to the database according to the given imageLocation in the file and id
     * @param imgLocation
     * @param recordid
     * @return
     */
    public long addImage(String imgLocation, Long recordid){
    	if (recordid==null)
    		recordid = -1L;
    	ContentValues args = new ContentValues();
    	args.put(KEY_FILENAME, imgLocation);
    	args.put(KEY_RECORD_ID, recordid);
    	return db.insert(IMAGES_TABLE, null, args);
    }
    /**
     * Gets the number of fields in the main table
     * @return
     */
    public long rowCount(){
    	Cursor c= db.query(MAIN_TABLE, new String [] {KEY_ROWID},
    			null, null, null, null, null);
    	return c.count();
    	
    }
    /**
     * Gets the count of the last image in the image table
     * @return
     */
    public int lastImage(){
    	Cursor c= db.query(IMAGES_TABLE, new String [] {KEY_ROWID},
    			null, null, null, null, null);
    	return c.count();
    }
    
    /**
     * Returns all the images associated with a certain ID
     * @param id
     * @return
     */
    public Cursor findImages(Long id ){
    	return db.query(IMAGES_TABLE, new String [] { KEY_ROWID, KEY_FILENAME, KEY_RECORD_ID}
    	        , KEY_RECORD_ID+"="+id, null, null, null, null);
    }
    
    /**
     * Return all the Images
     * @return
     */
    public Cursor allImages(){
    	return db.query(IMAGES_TABLE, new String [] { KEY_ROWID, KEY_FILENAME, KEY_RECORD_ID}
    	, null, null, null, null, null);
    	
    }
   /**
    * Returns all the names of the items in the database
    * @return
    */
    public Cursor allNames(){
    	return db.query(MAIN_TABLE, new String[]{KEY_NAME}, null, null, null, null, null);
    }
    /**
     * 
     * @param rowId
     * @return Id for the find associated with the given image
     */
    public String getIdForImage(Long rowId){
    	Cursor c =db.query(MAIN_TABLE, new String[]{KEY_IDENTIFIER}, KEY_ROWID+"="+rowId, null, null, null, null);
    	return c.getString(c.getColumnIndex(KEY_IDENTIFIER));
    }
    
    /**
     * Sets the description for the individual find
     * @param rowId
     * @param description
     * @return
     */
    public boolean setDescription(long rowId,String description){
    	ContentValues args = new ContentValues();
    	args.put(DBHelper.KEY_DESCRIPTION, description);
    	return db.update(IMAGES_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
    
    
    /**
     * Add sightings based on the 
     * @param latitude latitude value @type Double they're intended to be null if no device is detected
     * @param longitude longitude value @type Double
     * @param typeOfSighting type of sighting. How it was seen by the observer.. using telemetry, radio or whatever
     * @param description Description of the sighting, intended to be optional but will be useful nonetheless
     * @return
     */
    public long addSighting(Double latitude, Double longitude, String typeOfSighting, String description) {
    	ContentValues args = new ContentValues();
    	args.put(KEY_LATITUDE, latitude);
    	args.put(KEY_LONGITUDE, longitude);
    	args.put(KEY_OBSERVED,typeOfSighting);
    	args.put(KEY_DESCRIPTION, description);
    	return db.insert(SIGHTINGS_TABLE, null, args);
    }
    
    /**
     * Updating the values of the sightings
     * @param rowId - row Id of sighting that we want to change attributes of
     * @param latitude
     * @param longitude
     * @param typeOfSighting
     * @param description
     * @return
     */
    public boolean updateSighting(long rowId, Double latitude, Double longitude, String typeOfSighting, String description) {
    	ContentValues args= new ContentValues();
    	args.put(KEY_LATITUDE, latitude);
    	args.put(KEY_LONGITUDE, longitude);
    	args.put(KEY_OBSERVED,typeOfSighting);
    	args.put(KEY_DESCRIPTION, description);
    	return db.update(SIGHTINGS_TABLE, args, KEY_ROWID+"="+rowId,null)>0;
    }
    
    /**
     * Associates a given sighting with a find in the table from it's record id.
     * Record id could be different depending on the POSIT application.
     * @param rowId
     * @param recordid
     * @return
     */
    public boolean associateFind(long rowId, String recordid) {
    	ContentValues args = new ContentValues();
    	/*needs to be changed to just string*/
    	args.put(KEY_RECORD_ID, Integer.parseInt(recordid));
    	return db.update(SIGHTINGS_TABLE, args, KEY_ROWID+"="+rowId,null)>0;
    }
    
    /**
     * Temporary function used when updating/adding new tables
     */
    public  void createTables() {
    	this.db.execSQL(TABLE_SIGHTINGS_CREATE);
    }
    /**
     * @param rowId the Row Id of the item that needs to be updated
     * @param values HashMap of all the items we want to enter as 
     * key, value pairs and 
     * @return true or false depending on our result
     */
    public boolean updateSighting (long rowId, HashMap<String,String> values) {
    	Iterator<String> iter = values.keySet().iterator();
    	ContentValues args = new ContentValues();
    	while (iter.hasNext()) {
    		String key = iter.next();
    		String value = values.get(key);
    		if (key.equals(KEY_LATITUDE) || key.equals(KEY_LONGITUDE))
    			args.put(key, Double.parseDouble(value));
    		else if (key.equals(KEY_RECORD_ID))
    			args.put(key, Integer.parseInt(value));
    		else 
    			args.put(key, value);
    	}
    	
    	return db.update(SIGHTINGS_TABLE, args, KEY_ROWID+"="+rowId, null)>0;
    	
    }
    /**
     * Adds sightings based on the provided HashMap of Key, Value pairs
     * @param values
     * @return
     */
    public long addSighting (HashMap<String,String> values) {
    	Iterator<String> iter = values.keySet().iterator();
    	ContentValues args = new ContentValues();
    	while (iter.hasNext()) {
    		String key = iter.next();
    		String value = values.get(key);
    		Log.i(APP, "key ="+key+"Value= "+value);
    		
    		if (key.equals(KEY_LATITUDE) || key.equals(KEY_LONGITUDE))
    			args.put(key, Double.parseDouble(value));
    		else if (key.equals(KEY_RECORD_ID))
    			args.put(key, Integer.parseInt(value));
    		else 
    			args.put(key, value);
    	}
    	return db.insert(SIGHTINGS_TABLE, null, args);
    }
    
    public long addSighting(double latitude, double longitude, String time, Integer recordid,
    		String observation) {
    	ContentValues args = new ContentValues();
    	args.put(KEY_LATITUDE,latitude);
    	args.put(KEY_LONGITUDE, longitude);
    	args.put(KEY_TIME,time);
    	args.put(KEY_OBSERVED,observation );
    	return db.insert(SIGHTINGS_TABLE, null, args);
    }
    /**
     * Gets Id for a given Adapter Position, if it's not directly linked by CursorAdapter
     * or something similar as is the case with Spinners sometimes.
     * @param position
     * @return
     */
    public long getIdForAdapterPosition(int position) {
    	long result=-1L;
    	try {
    	Cursor c = db.rawQuery("SELECT "+KEY_ROWID+" FROM "+ MAIN_TABLE+ 
    			" LIMIT 1 OFFSET "+position, null);
    	if (c.next())
    		result= c.getLong(c.getColumnIndex(KEY_ROWID));
    	else 
    		result= -1;
    	}catch (IllegalStateException e){
    		
    	}
    	return result;
    	
    }
    
    public Cursor getSightings(long recordId,String[] Projection) {
    	return db.query(SIGHTINGS_TABLE,Projection,KEY_RECORD_ID+"="+recordId,null,null,null,null);
    }
}


