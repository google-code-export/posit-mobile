<?php
/*
	
	POSIT Web
	@author: Jwalanta Shrestha
	
	Build Project (apk + tables)
	
*/

require_once("includes/bootstrap.php");
if (!$isLogged) pageRedirect('index.php');

$id = cleanup($_GET['id']);
if ($id=='') pageRedirect('projects.php');

// check whether this id is valid and belongs to current user
$result = mysql_query("SELECT * FROM projects WHERE id='$id'");

if ($result){
	$row = mysql_fetch_assoc($result);
	if ($row['user_id']!=$currentUser['id']) pageRedirect('projects.php');
}
else pageRedirect('projects.php');

$pageTitle .= " - Project Build";
include "includes/pageheader.php";

?>
<div id='newsbody'>
<div class='container'>

<?php

// check if table already exists
$findtable = 'finds_'.$id.'_'.preg_replace('/[^a-z0-9_]/','',str_replace(" ","_",strtolower($row['name'])));

// generate and update Application Key
$app_key = md5($findtable);

$result = mysql_query("SHOW TABLES LIKE '$findtable'");

if (mysql_num_rows($result)>0 && !isset($_GET['nonag'])){
	// table exists but nonag is not set
	// so, warn user
	echo "<h3 style='color:red;'>Project find-table already exists. Recreating table will result in loss of previous data. Continue?</h3>\n";
	echo "<a href='project_build.php?id=$id&nonag' class='boxed'>Yes</a>\n";
	echo "<a href='projects.php' class='boxed'>No</a>\n";
}
else{
	// clear to delete (if required) and create table;
	
	// update to table
	mysql_query("UPDATE projects SET app_key='$app_key' WHERE id='$id'");
	echo "<p>Application Key: ",$app_key, "</p>";
	
	// delete table (no mercy, muhahahaha..)
	mysql_query("DROP TABLE IF EXISTS $findtable");
	
	$result = mysql_query("SELECT `project_field`.`name` as `name`, `fields`.`datatype` as `datatype` FROM project_field JOIN fields ON fields.id=project_field.field_id WHERE project_field.project_id='$id'");
	
	$createsyntax = "CREATE TABLE `$findtable` ";
	
	// default fields list
	$fieldslist = array('`id` int unsigned NOT NULL auto_increment', 
						'`project_id` int unsigned NOT NULL',
						'`device_id` int unsigned NOT NULL',
						'`latitude` float NOT NULL',
						'`longitude` float NOT NULL',
						'`find_time` datetime NOT NULL',
						'`description` text NOT NULL',
						'`revision` int unsigned DEFAULT 1'
						);
						
	while ($row = mysql_fetch_assoc($result)){
		$fieldslist[]=(strtolower("`".$row['name'])."` ".$row['datatype']);
	}
	
	// extra fields
	$fieldslist[] = '`post_data` text NOT NULL';	// save the whole post data
	$fieldslist[] = '`created_at` datetime NOT NULL';	// created timestamp
	$fieldslist[] = '`updated_at` datetime NOT NULL';	// created timestamp
	
	// default index list
	$fieldslist[] = 'PRIMARY KEY (`id`)';
	$fieldslist[] = 'KEY `latitude` (`latitude`,`longitude`)';
	$fieldslist[] = 'KEY `find_time` (`find_time`)';
	$fieldslist[] = 'FULLTEXT KEY `description` (`description`)';

	
	$createsyntax.=("(\n\t".implode(",\n\t",$fieldslist)."\n) ");
	
	echo "<h3>Building Tables..</h3>\n";
	$result = mysql_query($createsyntax);
	echo "<pre>$createsyntax</pre>";
	
	if ($result) echo "<p>Table Created.</p>";
	else echo "<p style='color:red;'>Table build failed!</p>";

}


?>
</div>
</div>

<?php
include "includes/pagefooter.php";
?>
