<?php
if(file_exists("config.php"))
	require_once("config.php");
else
	die("[Error at server, main.php line 5]No configuration file found. Follow the instructions in config.sample.php and try again.");
require_once("util/util.php");
require_once("util/errors.php");
require_once("util/constants.php");
require_once("util/log.php");
require_once("model/dao.php");
require_once("3rd/smarty-2.6.26/Smarty.class.php");
require_once("customPosit.php");

$dao = new DAO();
$smarty = new Smarty();
$smarty->template_dir = 'view/template/';
$smarty->compile_dir = 'cache/compile';
$smarty->cache_dir = 'cache';

require_once("util/auth.php");


?>