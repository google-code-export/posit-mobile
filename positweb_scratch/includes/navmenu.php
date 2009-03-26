<a href='index.php'>Home</a> | 
<?php
if ($isLogged == true) echo "<a href='projects.php'>My Projects</a> | ";
?>
<a href='news.php'>News</a> | 
<a href='about.php'>About</a> | 
<?php
if ($isLogged == true) echo "<a href='login.php'>Logout</a>"; 
else echo "<a href='login.php'>Login / Register</a>";
?>
