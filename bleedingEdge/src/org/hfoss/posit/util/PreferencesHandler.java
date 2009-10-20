package org.hfoss.posit.util;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;
/**
 * Parses the preferences.xml file
 * @author pgautam
 *
 */
public class PreferencesHandler extends DefaultHandler{
	private boolean in_application=false;
	private boolean in_name=false;
	private boolean in_type=false;
	private boolean in_version=false;
	private boolean in_instances=false;
	private boolean in_dbname=false;
	private boolean in_server=false;
	private boolean in_customized=false;
	private boolean in_instance_name=false;
	private boolean in_record_name=false;
	private boolean in_run=false;
	private final  String APP = "LocationNameHandler";
	/**
	 * just uses default
	 */
	@Override
	public void endDocument() throws SAXException {
		// TODO Auto-generated method stub
		super.endDocument();
	}
	
	/**
	 * sets the vars to false
	 */
	@Override
	public void endElement(String namespaceURI, String localName, String qName)
			throws SAXException {
		if (localName.equals("application")) 
			this.in_application= false;
		else if (localName.equals("type"))
			this.in_type=false;
		else if (localName.equals("name"))
				this.in_name=false;
		else if (localName.equals("version"))
				this.in_version=false;
		else if (localName.equals("instances"))
			this.in_instances =false;
		else if (localName.equals("dbname"))
			this.in_dbname = false;
		else if (localName.equals("server"))
			this.in_server = false;
		else if (localName.equals("customized"))
			this.in_customized = false;
		else if (localName.equals("instance_name"))
			this.in_instance_name = false;
		else if (localName.equals("record_name"))
			this.in_record_name = false;
		else if (localName.equals("run"))
			this.in_run = false;
	}

	@Override
	public  void startDocument() throws SAXException {
		
		super.startDocument();
	}

	
	@Override
	public void startElement(String namespaceURI, String localName,
            String qName, Attributes atts) throws SAXException {
		if (localName.equals("application")) 
			this.in_application=true;
		else if (localName.equals("type"))
			this.in_type=true;
		else if (localName.equals("name"))
				this.in_name=true;
		else if (localName.equals("version"))
				this.in_version=true;
		else if (localName.equals("instances"))
			this.in_instances =true;
		else if (localName.equals("dbname"))
			this.in_dbname = true;
		else if (localName.equals("server"))
			this.in_server = true;
		else if (localName.equals("customized"))
			this.in_customized = true;
		else if (localName.equals("instance_name"))
			this.in_instance_name = true;
		else if (localName.equals("record_name"))
				this.in_record_name = true;
		else if (localName.equals("run"))
			this.in_run = true;
	}

/**
 * 
 */
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		String parsedString = new String(ch, start, length);
		Log.i(APP,parsedString);
		if (this.in_type)
			DataSet.setType(parsedString);
		else if (this.in_version)
			DataSet.setVersion(parsedString);
		else if (this.in_name)
			DataSet.setName(parsedString);
		else if (this.in_instances)
			DataSet.setMultipleInstances(parsedString.equalsIgnoreCase("many")?true:false);
		else if (this.in_dbname)
			DataSet.setDbName(parsedString);
		else if (this.in_server)
			DataSet.setServer(parsedString);
		else if (this.in_customized)
			DataSet.setCustomizedBy(parsedString);
		else if (this.in_instance_name)
			DataSet.setInstanceName(parsedString);
		else if (this.in_record_name)
			DataSet.setRecordName(parsedString);
		else if (this.in_run)
			DataSet.setCleanRun(parsedString.equalsIgnoreCase("clean")?true:false);
	}

	
	

}
