<?php
/**
* Project:		positweb
* File name:	auth.php
* Description:	authorization functions and constants
* PHP version 5, mysql 5.0
*
* LICENSE: This source file is subject to LGPL license
* that is available through the world-wide-web at the following URI:
* http://www.gnu.org/copyleft/lesser.html
*
* @author       Antonio Alcorn
* @copyright    Humanitarian FOSS Project@Trinity (http://hfoss.trincoll.edu), Copyright (C) 2009.
* @package		posit
* @subpackage
* @tutorial
* @license  http://www.gnu.org/copyleft/lesser.html GNU Lesser General Public License (LGPL)
* @version
*/

function checkAuthn() {
	global $dao;
	if($_POST['doLogin']) {
		if($login = $dao->checkLogin($_POST['loginEmail'], $_POST['loginPass'])) {
			$_SESSION['loggedIn'] = true;
			$_SESSION['loginEmail'] = $_POST['loginEmail'];
			$_SESSION['loginId'] = $login['id'];
			return AUTHN_OK;
		} else
			return AUTHN_FAILED;
	} else
		return AUTHN_NOTLOGGEDIN;
}

function isLoggedIn() {
	return $_SESSION['loggedIn']? true: false;
}

function checkAuthz($action) {
	$noAuth = array('login', 'login.do', 'logout', 'main', '', 'register', 'register.do');
	if(in_array($action, $noAuth)) return true;
	return isLoggedIn();
}

?>