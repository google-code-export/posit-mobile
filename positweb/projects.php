<?php
/*
	
	POSIT Web
	@author: Jwalanta Shrestha
	
	Page to add/edit user's projects
	
*/

require_once("includes/bootstrap.php");
if (!$isLogged) pageRedirect('login.php');

// Edit Column (ajax backend for in-place editing)
if ($_GET['r']=='editcol'){
	$id = cleanup($_POST['id']);
	$value = cleanup($_POST['value']);
	
	$idarr = explode('_',$id);
	
	$query = "UPDATE `projects` SET `{$idarr[1]}`='$value' WHERE `id` = '{$idarr[2]}'";
	mysql_query($query);
	
	die($value);
	exit();
	
}

// Add new Project
if ($_GET['r']=='add'){
	$name = cleanup($_POST['name']);
	$desc = cleanup($_POST['desc']);
	$mysqltime = date ("Y-m-d H:i:s", time());
	
	$query = "INSERT INTO projects(`user_id`, `name`, `desc`,`created_at`) values ('{$currentUser['id']}','$name', '$desc','$mysqltime')";
	$query=str_replace("''","null",$query);
	
	mysql_query($query);
	
	pageRedirect("projects.php");
}

// Delete project
if ($_GET['r']=='delete'){
	$rowid=cleanup($_GET['id']);
	
	$result = mysql_query("SELECT * FROM projects WHERE id='$rowid'");
	if ($result) $row = mysql_fetch_assoc($result);
	
	$findtable = 'finds_'.$id.'_'.preg_replace('/[^a-z0-9_]/','',str_replace(" ","_",strtolower($row['name'])));
	mysql_query("DROP TABLE IF EXISTS $findtable");
	
	mysql_query("DELETE FROM projects WHERE id='$rowid'");
	mysql_query("DELETE FROM project_field WHERE project_id='$rowid'");
	
	pageRedirect("projects.php");
	
}

$pageTitle .= " - Projects";

$extrajs = array($jqueryPath,"js/jquery.jeditable.mini.js");
include "includes/pageheader.php";

?>
<div id='newsbody'>
<div class='container'>

<?php


	// show all projects
	
	echo "<h2>My Projects</h2>\n";
	
	$result = mysql_query("SELECT * FROM projects WHERE user_id='{$currentUser['id']}' ORDER BY created_at");
	while ($row = mysql_fetch_assoc($result)){
		echo "<div class='projects'><a href='project_build.php?id={$row['id']}' title='Build {$row['name']}'><img src='images/build_button.png' class='right' width='100px' /></a>\n";
		echo "<div class='project-title'><span class='field_column' id='field_name_{$row['id']}'>{$row['name']}</span></div>\n";
		echo "<div class='project-desc'><span class='field_column' id='field_desc_{$row['id']}'>{$row['desc']}</span></div>\n";
		echo "<div class='project-footer'><a href='project_prefs.php?id={$row['id']}'>Preferences</a> | <a href='project_fields.php?id={$row['id']}'>Fields</a> | <a href='projects.php?id={$row['id']}&r=delete' onclick='return confirm(\"Deleting project will remove all find data too. Are you sure?\");' >Delete</a> | <a href='finds_browse.php?id={$row['id']}'>Finds</a> | <a href='finds_map.php?id={$row['id']}'>Map</a> <br />[App Key: {$row['app_key']}]</div>\n";
		echo "</div>\n";
	}
	
	// display create new project form
	echo "<form method='POST' action='",$_SERVER['PHP_SELF'],"?r=add'>\n";
	echo "<div class='projects' style='border:none;'>\n";
	echo "<h3>Create New Project</h3>\n";
	insertTextBox('name','Title',20,50);
	insertTextBox('desc','Description',50,500);
	echo "<div class='inputbox'><input type='submit' value='Create'></div>";
	echo "</div>\n";
	echo "</form>\n";	
	
?>

</div>
</div>

<script>

/*

$(document).ready(function() { 
 
	// in-place editor
	$('.field_column').editable("projects.php?r=editcol", {
		indicator : 'Saving...',
		tooltip   : 'Click to edit...',
		submit: ' Ok ',
		height: '20px'
	});
	
});
	
*/

</script>

<?php

include "includes/pagefooter.php";

?>