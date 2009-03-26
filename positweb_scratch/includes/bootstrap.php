<?php

require_once("includes/vars.php");
require_once("includes/utils.php");

// connect to db

$dblink = dbConnect();

// check if user is already logged on
checkLogged();


?>