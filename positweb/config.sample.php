<?php
// Copy this to config.php and edit.

// base uri of server instance
define(SERVER_BASE_URI, "http://turing.cs.trincoll.edu/~aalcorn/positweb");
// database hostname
define("DB_HOST", "mysql.posit.ossf.org");
// database username
define("DB_USER", "posit_dev");
// database password
define("DB_PASS", "dg6TR9ar3GX2");
// database name
define("DB_NAME", "posit_dev_new");

/* dg6TR9ar3GX2 */

//enable if you want to keep the debug symbols on
// note: very insecure
define("DEBUG", true);

// enable if you want to keep log
// @todo need to do this in many levels - think Log4J
define("LOG", true);
if (LOG){
define("LOG_FILE", "/where/you/want/log/file");
}

define ("GOOGLE_MAPS_KEY", "YOUR_MAPS_API_KEY");

define ("POSIT_ANDROID_CODE", "/build_dir/posit-android");
define ("ANDROID_SDK_DIR", "/build_dir/android-sdk");
define ("ANT_EXEC", "/usr/bin/ant");

?>