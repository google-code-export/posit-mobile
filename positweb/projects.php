<?php
/*
	
	POSIT Web
	@author: Jwalanta Shrestha
	
	Page to add/edit user's projects
	
*/

require_once("includes/bootstrap.php");

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
	
	$query = "INSERT INTO projects(`user_id`, `name`, `desc`,`created`) values ('{$currentUser['id']}','$name', '$desc','$mysqltime')";
	$query=str_replace("''","null",$query);
	
	mysql_query($query);
	
	pageRedirect("projects.php");
}

// Delete project
if ($_GET['r']=='delete'){
	$rowid=cleanup($_GET['id']);
	mysql_query("DELETE FROM projects WHERE id='$rowid'");
	
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
	
	$result = mysql_query("SELECT * FROM projects WHERE user_id='{$currentUser['id']}' ORDER BY created");
	while ($row = mysql_fetch_assoc($result)){
		echo "<div class='projects'>\n";
		echo "<div class='project-title'><span class='field_column' id='field_name_{$row['id']}'>{$row['name']}</span></div>\n";
		echo "<div class='project-desc'><span class='field_column' id='field_desc_{$row['id']}'>{$row['desc']}</span></div>\n";
		echo "<div class='project-footer'><a href='projectedit.php?id={$row['id']}'>Manage Fields</a> | <a href='projects.php?id={$row['id']}&r=delete' onclick='return confirm(\"Are you sure?\");' >Delete</a></div>\n";
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
$(document).ready(function() { 
 
	// in-place editor
	$('.field_column').editable("projects.php?r=editcol", {
		indicator : 'Saving...',
		tooltip   : 'Click to edit...',
		submit: ' Ok ',
		height: '20px'
	});
	
});
	
</script>

<?php

include "includes/pagefooter.php";

?>