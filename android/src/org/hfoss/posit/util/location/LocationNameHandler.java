package org.hfoss.posit.util.location;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;
/**
 * A handler for the SAX parser for the locationd data from Google.
 * @author pgautam
 *
 */
public class LocationNameHandler extends DefaultHandler{
	private DataSet locationData= new DataSet();
	private boolean in_address=false;
	private boolean in_placename=false;
	private boolean in_state=false;
	private boolean in_countryCode=false;
	private final  String APP = "LocationNameHandler";
	@Override
	public void endDocument() throws SAXException {
		// TODO Auto-generated method stub
		super.endDocument();
	}
	
	public String getParsedData() {
		return this.locationData.toString();
	}
	
	@Override
	public void endElement(String namespaceURI, String localName, String qName)
			throws SAXException {
		if (localName.equals("address")) 
			this.in_address= false;
		else if (localName.equals("placename"))
			this.in_placename=false;
		else if (localName.equals("adminCode1"))
				this.in_state=false;
		else if (localName.equals("countryCode"))
				this.in_countryCode=false;
	}

	@Override
	public void startDocument() throws SAXException {
		
		super.startDocument();
	}

	
	@Override
	public void startElement(String namespaceURI, String localName,
            String qName, Attributes atts) throws SAXException {
		if (localName.equals("address")) 
			this.in_address=true;
		else if (localName.equals("placename"))
			this.in_placename=true;
		else if (localName.equals("adminCode1"))
				this.in_state=true;
		else if (localName.equals("countryCode"))
				this.in_countryCode=true;		
	}


	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		String parsedString = new String(ch, start, length);
		Log.i(APP,parsedString);
		if (this.in_placename)
			locationData.setCity(parsedString);
		else if (this.in_countryCode)
			locationData.setCountry(parsedString);
		else if (this.in_state)
			locationData.setState(parsedString);
	}

	
	
	private class DataSet{
		private String city, state, country;

		public DataSet() {
			
		}
		/**
		 * @return the country
		 */
		public String getCountry() {
			return country;
		}

		/**
		 * @param country the country to set
		 */
		public void setCountry(String country) {
			this.country = country;
		}

		/**
		 * @return the city
		 */
		public String getCity() {
			return city;
		}

		/**
		 * @param city the city to set
		 */
		public void setCity(String city) {
			this.city = city;
		}

		/**
		 * @return the state
		 */
		public String getState() {
			return state;
		}

		/**
		 * @param state the state to set
		 */
		public void setState(String state) {
			this.state = state;
		}
		
		public String toString() {
			/*String name = this.city+((this.state!=null)?",":""+this.state)
				+((this.country!=null)?",":"")+this.country;*/
			String name = this.city+","+this.state+","+this.country;
			Log.i(APP,name);
			return name;
		}
	}
}
