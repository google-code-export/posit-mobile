<div id='newsbody'>
<div class='container'>
<h2>News / Updates</h2>

<?php

$result = mysql_query("SELECT * FROM news ORDER BY created_at DESC LIMIT 0,4");
while ($row = mysql_fetch_assoc($result)){
	echo "<div class='news'>\n";
	echo "<div class='news-title'><a href='news.php?id={$row['id']}'>{$row['title']}</a></div>\n";
	echo "<div class='news-teaser'>{$row['teaser']} <a href='news.php?id={$row['id']}'>More..</a></div>\n";
	$user = getUserInfo($row['user_id']);
	echo "<div class='news-footer'>Created by <em>",$user['user'],"</em> on <em>",date("m.d.y",strtotime($row['created_at'])),"</em></div>\n";
	echo "</div>\n";
}


?>

</div>
</div>