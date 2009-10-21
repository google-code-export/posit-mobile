<?php

require_once("controller/main.php");
require_once("controller/web.php");

$path = explode("/", $_SERVER["REQUEST_URI"]);
for($n=0; $n<count($path); $n++)
	if($path[$n] == 'web')
		break;

$p = "";
$i = $n + 1;
while(isset($path[$i])) {
	$p .= "/".$path[$i];
	$i++;
}

webController($p, $_REQUEST);


?>