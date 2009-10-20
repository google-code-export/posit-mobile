<?php

/*
	
	POSIT Web
	@author: Jwalanta Shrestha
	
	Register Data Collecting Devices (Android phones)
	
*/

require_once("includes/bootstrap.php");

$appkey = cleanup($_POST['app_key']);
$imei = cleanup($_POST['imei']);
$name = cleanup($_POST['name']);

// check if app key is valid and get project id for it
if ($appkey=='' || strlen($appkey)!=32) die('{"status":"","error":"Invalid Application Key"}');

$result = mysql_query("SELECT * FROM projects WHERE app_key='$appkey'");
if (mysql_num_rows($result)>0){
	$row = mysql_fetch_assoc($result);
	$id = $row['id'];
	
	if ($imei=='') die('{"status":"","error":"Invalid IMEI Number"}');
	if ($name=='') die('{"status":"","error":"Invalid Name"}');
	
	// everything's okay, lets register the device
	$result = mysql_query("INSERT INTO devices(`project_id`, `name`, `imei`) values('$id','$name','$imei')");
	if ($result) die('{"status":"OK","error":""}'); else die('{"status":"","error":"Registration Failed"}'.mysql_error());
}
else{
	die('{"status":"","error":"Application Key doesn\'t exist"}');
}
