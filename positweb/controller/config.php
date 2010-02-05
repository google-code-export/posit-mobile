<?php
// Copy this to config.php and edit.

// base uri of server instance
define(SERVER_BASE_URI, "http://localhost/posit");
// database hostname
define("DB_HOST", "localhost");
// database username
define("DB_USER", "root");
// database password
define("DB_PASS", "root");
// database name
define("DB_NAME", "posit_web");

/* dg6TR9ar3GX2 */

//enable if you want to keep the debug symbols on
// note: very insecure
define("DEBUG", true);

// enable if you want to keep log
// @todo need to do this in many levels - think Log4J
define("LOG", true);
if (LOG){
define("LOG_FILE", "/log/");
}

define ("GOOGLE_MAPS_KEY", "ABQIAAAAUlJap888YXnyJ0x-3SD9JxROxdxrcLD3lYp40t9TM89VIkUnWRRc_5dMwOIgoN9RSP_iwkdVaejk5g");

define ("POSIT_ANDROID_CODE", "/build_dir/posit-android");
define ("ANDROID_SDK_DIR", "/build_dir/android-sdk");
define ("ANT_EXEC", "/usr/bin/ant");

?>