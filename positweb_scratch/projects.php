<?php

require_once("includes/bootstrap.php");

if ($_GET['r']=='add'){
	$name = cleanup($_POST['name']);
	$desc = cleanup($_POST['desc']);
	
	$query = "INSERT INTO projects(`user_id`, `name`, `desc`,`created`) values ('{$currentUser['id']}','$name', '$desc')";
	
	mysql_query($query);
	
	pageRedirect("projects.php");
}

if ($_GET['r']=='delete'){
	$rowid=cleanup($_GET['id']);
	mysql_query("DELETE FROM projects WHERE id='$rowid'");
	
	pageRedirect("projects.php");
	
}

$pageTitle .= " - Projects";

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
		echo "<div class='project-title'><a href='projectedit.php?id={$row['id']}'>{$row['name']}</a></div>\n";
		echo "<div class='project-desc'>{$row['desc']}</div>\n";
		echo "<div class='project-footer'><a href='projectedit.php?id={$row['id']}'>Edit</a> | <a href='projects.php?id={$row['id']}&r=delete'>Delete</a></div>\n";
		echo "</div>\n";
	}
	
	
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
<?php

include "includes/pagefooter.php";

?>