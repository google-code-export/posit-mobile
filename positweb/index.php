<?php
// Check if the configuration file exists. If so, proceed to the main controller.
if(file_exists("config.php")) {
	header("Location: web/main");
// If not, run the install wizard.
} else {
	require("install.php");
}
?>