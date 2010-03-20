<?php

// Check if the configuration file exists. If it does, the server has already
// been installed and configured, and we should exit.

if(file_exists("config.php")) {
	echo "<h1>Already configured</h1>
	<p>The server software appears to already have been installed and configured.</p>";
	exit();
}

$base_uri_guess = "http://".
	rtrim($_SERVER["HTTP_HOST"]. $_SERVER["REQUEST_URI"], "/");

extract($_POST);
if($action == "install") {
	if(!($base_uri && $db_host && $db_user && $db_pass)) {
		echo "Please make sure to fill out everything, and try again.";
		exit();
	} else {
		$config_template = file_get_contents("config.template.inc.php");
		$config_template = str_replace("##SERVER_BASE_URI##", $base_uri, $config_template);
		$config_template = str_replace("##DB_HOST##", $db_host, $config_template);
		$config_template = str_replace("##DB_USER##", $db_user, $config_template);
		$config_template = str_replace("##DB_PASS##", $db_pass, $config_template);
		$config_template = str_replace("##DB_NAME##", $db_name, $config_template);
		$config_template = str_replace("##GOOGLE_MAPS_KEY##", $maps_key, $config_template);
		
		if(mysql_connect($db_host, $db_user, $db_pass)) {
			if(mysql_select_db($db_name)) {
				$sql = file_get_contents("setup/positweb.sql");
				$sql_statements = explode(";", $sql);
				$numSqlErrs = 0;
				foreach($sql_statements as $s) {
					if(trim($s)) {
						if(!mysql_query($s))
							$numSqlErrs++;
					}
				}
				if($numSqlErrs < 1) {


$rewrite_rules = <<< END


    RewriteEngine On
    RewriteBase /
    RewriteCond %{REQUEST_FILENAME} !-f
    RewriteCond %{REQUEST_FILENAME} !-d
    RewriteRule ^(.*)$ index.php?q=$1 [L,QSA]
END;
				$h = fopen(".htaccess", "a");
				fwrite($h, $rewrite_rules);
				fclose($h);
				
				$h = fopen("config.php", "w");
				fwrite($h, $config_template);
				fclose($h);
				
					echo <<< END
	<h1>Install successfull</h1>
	<p>Positweb was successfully installed. <a href="web/main">Click here to run it!</a>
END;
exit();
				} else {
					die("Mysql error: ". mysql_error());
				}
			} else {
				die("This database doesn't exist.");
			}
		} else {
			die("Error connecting to database server.");
		}
	}
}

?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en" dir="ltr">
	<head>
		<title>Install Positweb</title>
		<link rel="stylesheet" href="res/style/basic.css"/>
	</head>
	<body>
		<div id="header">
			<h1><span>Posit</span></h1>		
		</div>
		<div id="content"><h2>Install Positweb</h2>

<form action="install.php" method="post">
	<input type="hidden" name="action" value="install"/>
<fieldset>
	<legend>Web server settings</legend>
	<p>This is the URI where users will access Positweb. A best guess has been filled in.</p>
	<label for="base_uri">Server base URI:</label></td>
	<input type="text" name="base_uri" id="base_uri" size="50"
			value="<?php echo $base_uri_guess; ?>"/>
</fieldset>

<fieldset>
	<p>This is the information Positweb needs to connect to your database server.</p>
	<legend>Database settings</legend>
	<p>
	<label for="db_host">Database hostname:</label>
	<input type="text" name="db_host" value="localhost"/>
	</p>
	<p>
	<label for="db_host">Database username:</label>
	<input type="text" name="db_user" value=""/>
	</p>
	<p>
	<label for="db_pass">Database password:</label>
	<input type="password" name="db_pass" value=""/>
	</p>
	<p>
	<label for="db_host">Database name:</label>
	<input type="text" name="db_name" value=""/>
	</p>
</fieldset>

<fieldset>
	<legend>Google Maps</legend>
	<label for="maps_key">Google Maps API key</label>
	<input type="text" name="maps_key" id="maps_key" size="50"/>
</fieldset>
	<input type="submit" value="Install"/>
</form>

	</body>
</html>