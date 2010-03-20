<?php
// Copy this to config.php and edit.

// base uri of server instance
define(SERVER_BASE_URI, "##SERVER_BASE_URI##");
// database hostname
define("DB_HOST", "##DB_HOST##");
// database username
define("DB_USER", "##DB_USER##");
// database password
define("DB_PASS", "##DB_PASS##");
// database name
define("DB_NAME", "##DB_NAME##");

//enable if you want to keep the debug symbols on
// note: very insecure
define("DEBUG", false);

// enable if you want to keep log
// @todo need to do this in many levels - think Log4J
define("LOG", false);
if (LOG){
define("LOG_FILE", "/where/you/want/log/file");
}

define ("GOOGLE_MAPS_KEY", "##GOOGLE_MAPS_KEY##");

define ("POSIT_ANDROID_CODE", "/build_dir/posit-android");
define ("ANDROID_SDK_DIR", "/build_dir/android-sdk");
define ("ANT_EXEC", "/usr/bin/ant");

?>