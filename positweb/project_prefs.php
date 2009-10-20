<?php
/*
	
	POSIT Web
	@author: Jwalanta Shrestha
	
	Page to add/edit project's preferences
	
*/

require_once("includes/bootstrap.php");
if (!$isLogged) pageRedirect('index.php');

// save changes
if ($_GET['r']=='edit'){
	$id = cleanup($_POST['id']);
	$name = cleanup($_POST['name']);
	$desc = cleanup($_POST['desc']);
	
	$query = "UPDATE projects SET `name`='$name', `desc`='$desc' WHERE `id`='$id'";
	$query=str_replace("''","null",$query);
	
	mysql_query($query);

	pageRedirect("project_prefs.php?id=".$id);
}

$id = cleanup($_GET['id']);
if ($id=='') pageRedirect('projects.php');

// check whether this id is valid and belongs to current user

$result = mysql_query("SELECT * FROM projects WHERE id='$id'");

if ($result){
	$row = mysql_fetch_assoc($result);
	if ($row['user_id']!=$currentUser['id']) pageRedirect('projects.php');
}
else pageRedirect('projects.php');

$pageTitle .= " - Project Preferences";

$extrajs = array($jqueryPath);
include "includes/pageheader.php";

?>
<div id='newsbody'>
<div class='container'>

<?php

echo "<form method='POST' action='",$_SERVER['PHP_SELF'],"?r=edit'>\n";
echo "<div class='projects' style='border:none;'>\n";
echo "<h3>Create New Project</h3>\n";
echo "<input type='hidden' name='id' value='$id' />\n";
insertTextBox('name','Title',30,50,"value='{$row['name']}'");
insertTextBox('desc','Description',50,500,"value='{$row['desc']}'");
echo "<div class='inputbox'><input type='submit' value='Save'></div>";
echo "</div>\n";
echo "</form>\n";

?>

</div>
</div>

<?php

include "includes/pagefooter.php";

?>