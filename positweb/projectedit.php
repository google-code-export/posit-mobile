<?php

/*
	
	POSIT Web
	@author: Jwalanta Shrestha
	
	Page to add/edit fields in a project
	
*/


require_once("includes/bootstrap.php");

// ajax backend for field ordering
if ($_GET['r']=='order'){
	foreach ($_GET['fielditem'] as $position => $item) : 
	  $query = "UPDATE `project_field` SET `order` = '$position' WHERE `id` = '$item'"; 
	  mysql_query($query);
	endforeach; 

	die("Field arrangement saved.");
	exit();
}

// Edit Column (ajax backend for in-place editing)
if ($_GET['r']=='editcol'){
	$id = cleanup($_POST['id']);
	$value = cleanup($_POST['value']);
	
	$idarr = explode('_',$id);
	
	$query = "UPDATE `project_field` SET `{$idarr[1]}`='$value' WHERE `id` = '{$idarr[2]}'";
	mysql_query($query);
	
	die($value);
	exit();
	
}

// add new field
if ($_GET['r']=='add'){
	$name = cleanup($_POST['name']);
	$desc = cleanup($_POST['desc']);
	$field_id = cleanup($_POST['field_id']);
	$project_id = cleanup($_POST['project_id']);
	
	// get the latest order
	$result = mysql_query("SELECT max(`order`) as `order` FROM project_field WHERE project_id='$project_id'");
	if (mysql_num_rows($result)>0){
		$row = mysql_fetch_assoc($result);
		$order = ((int)$row['order'])+1;
	}
	else $order = 1;
	
	$query = "INSERT INTO project_field(`project_id`, `field_id`, `name`, `desc`, `order`) values ('$project_id','$field_id','$name', '$desc','$order')";
	$query=str_replace("''","null",$query);
	
	mysql_query($query);

	pageRedirect("projectedit.php?id=".$project_id);
}

// delete field
if ($_GET['r']=='delete'){
	$rowid=cleanup($_GET['id']);
	$project_id=cleanup($_GET['project_id']);
	mysql_query("DELETE FROM project_field WHERE id='$rowid'");
	
	pageRedirect("projectedit.php?id=".$project_id);
	
}

$id = $_GET['id'];
$id = cleanup($id);
if ($id=='') pageRedirect('projects.php');

$pageTitle .= " - Projects";

$extrajs = array($jqueryPath,$jqueryUIPath,"js/jquery.jeditable.mini.js");
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


// display all fields
echo "<div id='msg'></div>";
echo "<h2>{$project['name']}</h2>\n";
echo "<ul id='field-list'>\n";
$result = mysql_query("SELECT * FROM project_field WHERE project_id='$id' ORDER by `order`");
while ($row = mysql_fetch_assoc($result)){
	echo "<li id='fielditem_{$row['id']}'>";
	echo "<span class='right'>";
	echo "<a href='projectedit.php?id={$row['id']}&project_id=$id&r=delete' onclick='return confirm(\"Are you sure?\");'><img src='images/delete.png' border='0' title='Delete' /></a>";
	echo "</span>\n";
	echo "<img src='images/drag.png' class='handle' title='Click and drag to rearrange..'>\n";
	
	echo "<strong> <span class='field_column' id='field_name_{$row['id']}'>{$row['name']}</span></strong> \n";
	echo "<em> <span class='field_column' id='field_desc_{$row['id']}'>{$row['desc']}</span></em>";
	
	echo "</li>\n";
}	
echo "</ul>\n";


// show 'add new form' field
echo "<form method='POST' action='",$_SERVER['PHP_SELF'],"?r=add'>\n";
echo "<div class='projects' style='border:none;'>\n";
echo "<h3>Add Field</h3>\n";
insertTextBox('name','Title',20,50);
insertTextBox('desc','Description',50,500);
insertComboBox('field_id',"Type",$fields);
echo "<div class='inputbox'><input type='hidden' name='project_id' id='project_id' value='$id'><input type='submit' value='Add'></div>";
echo "</div>\n";
echo "</form>\n";


?>

</div>
</div>

<script>
$(document).ready(function() { 
	
	// field sortable
	$("#field-list").sortable({ 
		handle : '.handle', 
		update : function () { 
			
			var order = $('#field-list').sortable('serialize'); 
			var url = "projectedit.php?r=order&"+order;
			
			$("#msg").html("<img src='images/indicator.gif' />");
			
			$.get(url,{}, function(data){
				//alert(data);
				
				$("#msg").hide().html(data).show()
				setTimeout(function(){ 
					$("#msg").fadeOut("slow")
				},2000); 		
				
			});
			
		} 
	}); 

	// inplace editor
	$('.field_column').editable("projectedit.php?r=editcol", {
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