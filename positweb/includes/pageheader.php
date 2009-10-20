<html>
<head>
<title><?php echo $pageTitle; ?></title>
<link href="css/style.css" rel="stylesheet" type="text/css" />
<?php
if (isset($extracss)){
	foreach($extracss as $css){
		echo "<link href='$css' rel='stylesheet' type='text/css' />\n";
	}
}

if (isset($extrajs)){
	foreach($extrajs as $js){
		echo "<script type='text/javascript' src='$js'></script>\n";
	}
}
?>
</head>
<body>
<div id='header' style='background-color: #f1f1f1;'>
	<div class='container'>
	<div style='float:right'><?php include "includes/navmenu.php"; ?></div>
	<span class='headerlogo'><a href='index.php' title='POSIT'><img src='images/positlogo.png' border="0"></a></span> <br />Portable Open Search and Identification Tool
	
	</div>
</div>
<div class="clearboth"></div>
