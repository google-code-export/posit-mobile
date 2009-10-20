<?php
/*
	
	POSIT Web
	@author: Jwalanta Shrestha
	
	Misc. utility functions
	
*/

function dbConnect(){
	// connect to db
	
	require "includes/vars.php";
	
	$dblink = mysql_connect($dbserver, $dbusername, $dbpassword);
	if (!$dblink) die('Could not connect to MySQL server: ' . mysql_error());
	mysql_select_db($dbname, $dblink);	
	
    return $dblink;	

}

function checkLogged(){
	global $dblink, $currentUser, $currentUserGroup, $isLogged;
	
	//print_r($_COOKIE);
	if (isset($_COOKIE['posituser']) && isset($_COOKIE['positpass'])){
		// check if they are valid
		
		$result = mysql_query("SELECT * FROM users WHERE name='{$_COOKIE['posituser']}' AND password='{$_COOKIE['positpass']}'");
		if (mysql_num_rows($result)>0) {
			
			$currentUser = mysql_fetch_assoc($result);
			
			// fetch group info
			$result = mysql_query("SELECT * FROM groups WHERE id='{$currentUser['group_id']}'");
			$currentUserGroup = mysql_fetch_assoc($result);
			
			$isLogged = true;
			return true; 
		}
		else return false;
	}
}

function getUserInfo($id){
	$result = mysql_query("SELECT * FROM users WHERE id='$id'");
	$row = mysql_fetch_assoc($result);
	
	return $row;
}

function pageRedirect($page){
	header("Location: $page");
	exit();
}


/**
 * Inserts textbox
 *
 * @param $id Object ID
 * @param $label Label
 * @param $size Size (length, visible) of data
 * @param $maxlength Maximum length of data
 * @param $extraargs Extra arguments for the input field
 */
function insertTextBox($id, $label, $size, $maxlength, $extraargs=''){
	echo "<div class='inputbox'>\n";
	echo "<label>$label</label>";
	echo "<input type='text' name='$id' id='$id' size='$size' maxlength='$maxlength' title='$label' $fns $extraargs>\n";	
	echo "</div>\n";
}

function insertPasswordBox($id, $label, $size, $maxlength, $extraargs=''){
	echo "<div class='inputbox'>\n";
	echo "<label>$label</label>";
	echo "<input type='password' name='$id' id='$id' size='$size' maxlength='$maxlength' title='$label' $fns $extraargs>\n";	
	echo "</div>\n";
}

/**
 * Inserts Combobox
 *
 * @param $id Object ID
 * @param $label Label
 * @param $list List to be displayed (with key)
 * @param boolean $sort Sort bool
 * @param $extraargs Extra arguments for the input field 
 */
function insertComboBox($id, $label, $list, $sort = false, $extraargs=''){
	echo "<div class='inputbox'>\n";
	echo "<label>$label</label>";
	echo "<select id='$id' name='$id' title='$label' $extraargs>\n";

	if ($sort==true) asort($list);

	echo "<option value=''></option>\n";
	
	$k = array_keys($list);
	$v = array_values($list);
	for ($i=0;$i<count($list);$i++){
		printf("<option value='%s'>%s</option>\n",$k[$i],trim($v[$i]));
	}

	echo "</select>\n";
	echo "</div>\n";
}

function cleanup($data, $write=false) {
    if (is_array($data)) {
        foreach ($data as $key => $value) {
            $data[$key] = cleanup_lvl2($value, $write);
        }
    } else {
        $data = cleanup_lvl2($data, $write);
    }
    return $data;
}

function cleanup_lvl2($data, $write=false) {
    if (isset($data)) { // preserve NULL
        if (get_magic_quotes_gpc()) {
            $data = stripslashes($data);
        }
        if ($write) {
            $data = mysql_real_escape_string($data);
        }
    }
    return $data;
}

function get_user_agent(){
	return $_SERVER['HTTP_USER_AGENT'];
}
?>