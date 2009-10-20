<?php
/*
	
	POSIT Web
	@author: Jwalanta Shrestha
	
	Page to display news
	
*/

require_once("includes/bootstrap.php");

$id = $_GET['id'];
$id = cleanup($id);

$pageTitle .= " - News";

include "includes/pageheader.php";

?>
<div id='newsbody'>
<div class='container'>
<?php

if ($id==''){
	// show all news
	
	echo "<h2>News / Updates</h2>\n";
	
	$result = mysql_query("SELECT * FROM news ORDER BY created_at DESC");
	while ($row = mysql_fetch_assoc($result)){
		echo "<div class='news'>\n";
		echo "<div class='news-title'><a href='news.php?id={$row['id']}'>{$row['title']}</a></div>\n";
		echo "<div class='news-teaser'>{$row['teaser']} <a href='news.php?id={$row['id']}'>More..</a></div>\n";
		$user = getUserInfo($row['user_id']);
		echo "<div class='news-footer'>Created by <em>",$user['name'],"</em> on <em>",date("m.d.y",strtotime($row['created_at'])),"</em></div>\n";
		echo "</div>\n";
	}
}
else{
	// show only the selected one
	$result = mysql_query("SELECT * FROM news WHERE id='$id'");
	$row = mysql_fetch_assoc($result);
	
	echo "<div class='news' style='border:none;'>\n";
	echo "<div class='news-title'><a href='news.php?id={$row['id']}'>{$row['title']}</a></div>\n";
	echo "<div class='news-teaser'>{$row['teaser']}</div>\n";
	echo "<div class='news-teaser'>{$row['body']}</div>\n";
	$user = getUserInfo($row['user_id']);
	echo "<div class='news-footer'>Created by <em>",$user['name'],"</em> on <em>",date("m.d.y",strtotime($row['created_at'])),"</em></div>\n";
	echo "</div>\n";	
	
	
}
?>
</div>
</div>
<?php

include "includes/pagefooter.php";

?>