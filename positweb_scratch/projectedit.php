<?php

require_once("includes/bootstrap.php");

$id = $_GET['id'];
$id = cleanup($id);

if ($id=='') pageRedirect('projects.php');

if ($_GET['r']=='add'){
	$name = cleanup($_POST['name']);
	$desc = cleanup($_POST['desc']);
	$field_id = cleanup($_POST['field_id']);
	$project_id = cleanup($_POST['project_id']);
	
	$query = "INSERT INTO project_field(`project_id`, `field_id`, `name`, `desc`) values ('$project_id','$field_id','$name', '$desc')";
	
	mysql_query($query);
	pageRedirect("projectedit.php?id=".$project_id);
}

if ($_GET['r']=='delete'){
	$rowid=cleanup($_GET['id']);
	$project_id=cleanup($_GET['project_id']);
	mysql_query("DELETE FROM project_field WHERE id='$rowid'");
	
	pageRedirect("projectedit.php?id=".$project_id);
	
}

$pageTitle .= " - Projects";

include "includes/pageheader.php";

?>
<div id='newsbody'>
<div class='container'>

<?php


$result = mysql_query("SELECT * FROM projects WHERE id='$id'");
$project = mysql_fetch_assoc($result);

$result = mysql_query("SELECT * FROM fields");
$fields = array();
while ($row = mysql_fetch_assoc($result)) $fields[$row['id']]=$row['name'];

echo "<h2>{$project['name']}</h2>\n";
$result = mysql_query("SELECT * FROM project_field WHERE project_id='$id' ORDER by `order`");
while ($row = mysql_fetch_assoc($result)){
	echo "<div class='projects'>\n";
	echo "<div class='project-title'>{$row['name']}</div>\n";
	echo "<div class='project-desc'>{$row['desc']}</div>\n";
	echo "<div class='project-footer'><a href='projects.php?id=$id'>Edit</a> | <a href='projects.php?id={$row['id']}&project_id=$id&r=delete'>Delete</a></div>\n";
	echo "</div>\n";
}	


echo "<form method='POST' action='",$_SERVER['PHP_SELF'],"?r=add'>\n";
echo "<div class='projects' style='border:none;'>\n";
insertTextBox('name','Title',20,50);
insertTextBox('desc','Description',50,500);
insertComboBox('field_id',"Type",$fields);
echo "<div class='inputbox'><input type='hidden' name='project_id' value='$id'><input type='submit' value='Add'></div>";
echo "</div>\n";
echo "</form>\n";


?>

</div>
</div>
<?php

include "includes/pagefooter.php";

?>