<?php

function genAuthKey($length = 16) {
	$k = "";
	$chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
	
	for($n=0; $n<$length; $n++)
		$k .= $chars {
			rand(0, strlen($chars))
		};
	return $k;
}

?>