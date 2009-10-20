package org.hfoss.posit.util;

import java.io.InputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.hfoss.posit.R;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.content.Context;
import android.util.Log;
/**
 * It stores the information about the program that's parsed from the preferences.xml file.
 * @author pgautam
 *
 */
public class DataSet {
	/**
	 * @return the cleanRun
	 */
	public static boolean isCleanRun() {
		return cleanRun;
	}

	/**
	 * @param cleanRun the cleanRun to set
	 */
	public static void setCleanRun(boolean cleanRun) {
		DataSet.cleanRun = cleanRun;
	}

	private static String name,type,version;
	private static boolean multipleInstances;
	private static String DbName,server,customizedBy, instanceName, recordName;
	private static boolean cleanRun;
    /**
	 * @return the recordName
	 */
	public static String getRecordName() {
		return recordName;
	}

	/**
	 * @param recordName the recordName to set
	 */
	public static void setRecordName(String recordname) {
		recordName = recordname;
	}

	/**
	 * @return the instanceName
	 */
	public static String getInstanceName() {
		return instanceName;
	}

	/**
	 * @param instanceName the instanceName to set
	 */
	public static void setInstanceName(String instancename) {
		instanceName = instancename;
	}

	/**
	 * @return the customizedBy
	 */
	public static String getCustomizedBy() {
		return customizedBy;
	}

	/**
	 * @param customizedBy the customizedBy to set
	 */
	public static void setCustomizedBy(String customizedby) {
		customizedBy = customizedby;
	}

	/**
	 * @return the server
	 */
	public static String getServer() {
		return server;
	}

	/**
	 * @param server the server to set
	 */
	public static void setServer(String Server) {
		server = Server;
	}

	/**
     * Parses for the preferences from a 
     * res/raw/preferences.xml file and saves the content to the 
     * DataSet Structure so that it can be shared across the application
     * 
     * <b>Refactoring Notes</b> Might want to move it somewhere else to make code
     * simpler.
     */
    public static void getPOSITPreferences(Context context) {
    	/*SharedPreferences pref = getSharedPreferences ("Preferences",MODE_PRIVATE);
    	pref.getString("default Info", "test");*/
    	try {
			/*Get the preferences file, open it!*/
			InputStream is = context.getResources().openRawResource(R.raw.preferences);
			

			/* Get a SAXParser from the SAXPArserFactory. */
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();

			/* Get the XMLReader of the SAXParser we created. */
			XMLReader xr = sp.getXMLReader();
			InputSource inputSource = new InputSource();
			inputSource.setByteStream(is);
			/*
			 * Create a new ContentHandler and apply it to the XML-Reader
			 */
			PreferencesHandler ph = new PreferencesHandler();
			xr.setContentHandler(ph);
			
			/* Parse the xml-data our URL-call returned. */
			xr.parse(inputSource);
			is.close();
			/*Let's see if we have the data in*/
			Log.i("PositMain", DataSet.getName());
			Log.i("PositMain",DataSet.isMultipleInstances()+"");
			//TODO I'm getting println needs message error here. Need to fix that
    	} catch(Exception e) {
    		Log.e("DataSet", "err "+e.getMessage());
    	}
    	/*} catch (IOException e) {
    		Log.e(APP, "Can't read the file. Is it there?"+e.getMessage());
    	} catch (SAXException e) {
    		Log.e(APP, "Something is wrong with SAX"+e.getCause());
    	} catch (ParserConfigurationException e) {
    		Log.e(APP, "Something is wrong with Configuration"+e.getCause());
//    	} catch (NullPointerException e) {
    		Log.e(APP, "Fix that Null Pointer "+e.getStackTrace());
    	}*/
    	
    }

	/**
	 * @return the dbName
	 */
	public static String getDbName() {
		return DbName;
	}

	/**
	 * @param dbName the dbName to set
	 */
	public static void setDbName(String dbName) {
		DbName = dbName;
	}

	/**
	 * @return the multipleInstances
	 */
	public static boolean isMultipleInstances() {
		return multipleInstances;
	}

	/**
	 * @param multipleInstances the multipleInstances to set
	 */
	public static void setMultipleInstances(boolean givenmultipleInstances) {
		multipleInstances = givenmultipleInstances;
	}

	/**
	 * @return the name
	 */
	public static String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public static void setName(String givenname) {
		name = givenname;
	}

	/**
	 * @return the type
	 */
	public static String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public static void setType(String giventype) {
		type = giventype;
	}

	/**
	 * @return the version
	 */
	public static String getVersion() {
		return version;
	}

	/**
	 * @param version the version to set
	 */
	public static void setVersion(String givenversion) {
		version = givenversion;
	}
}
