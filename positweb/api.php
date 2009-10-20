<?php

require_once("controller/main.php");
require_once("controller/api.php");

$path = explode("/", $_SERVER["REQUEST_URI"]);
for($n=0; $n<count($path); $n++)
	if($path[$n] == 'api')
		break;
		
$p = "";
$i = $n + 1;
while(isset($path[$i])) {
	$p .= "/".$path[$i];
	$i++;
}

apiController($p, $_REQUEST);


?>