package org.hfoss.posit.db;

import android.net.Uri;
import android.provider.BaseColumns;

public class PositData {

	public static final class Photos implements BaseColumns{
		//TODO Copied from GeoPhoto Application, might need to change
		public static final Uri CONTENT_URI =  Uri.parse("content://" +
				"org.hfoss.posit.db.Posit/photos");

		public static final String DEFAULT_SORT_ORDER = "_id";

		public static final String NAME = "name";

		public static final String DESCRIPTION = "description";

		public static final String BITMAP = "_data";

//		public static final String LATITUDE = "latitude";
//
//		public static final String LONGITUDE = "longitude";
//
//		public static final String ALTITUDE = "altitude";
//
		public static final String CREATED_DATE = "created";
//
		public static final String MODIFIED_DATE = "modified";
	}
}
