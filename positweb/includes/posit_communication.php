<?php

/*
	
	POSIT Web
	@author: Jwalanta Shrestha
	
	POSIT Communication Class v1.1 ;)

*/

require_once("utils.php");

class Posit_communication{
	
	var $project_id = '';
	var $device_id = '';
	var $findtable = '';
	var $result = array();
	
	
	function Posit_communication(){
		$this->result['status'] = 'OK';
		$this->result['error'] = '';
	}
	
	//
	// creates a new find from POST data
	//
	function save(){
		
		global $_POST, $dblink;
		
		if (isset($_POST['id'])){
			return $this->edit();			
		}
		
		$dt = array();
		
		// fetch the fields
		$result = mysql_query("SELECT * FROM $this->findtable LIMIT 0,1");
	
		// get column metadata
		$i = 0;
		while ($i < mysql_num_fields($result)) {
		    $meta = mysql_fetch_field($result, $i);
		    if ($meta && $meta->name!='id') $dt[$meta->name]=cleanup($_POST[$meta->name]);
		    $i++;
		}
		
		// set project specific data
		$dt['project_id']=$this->project_id;
		$dt['device_id']=$this->device_id;
		$dt['find_time']= date("Y-m-d H:i:s",strtotime($dt['find_time']));
		
		// set extra data
		$dt['revision'] = 1;
		$dt['post_data'] = serialize($_POST);
		$dt['created_at'] = date("Y-m-d H:i:s", time());
		$dt['updated_at'] = $dt['created_at'];
	
		// create query
		$cols = implode(",",array_keys($dt));
		$values = implode("','", array_values($dt));
		$query = "INSERT INTO $this->findtable ($cols) VALUES('$values')";
		$query=str_replace("''","null",$query);
		
		$result = mysql_query($query);
		
		if ($result) {
			$this->set_find_status(mysql_insert_id());
		}
		else{			
			$this->set_error("DB Write error: ".mysql_error($dblink));
			return false;
		}		
		
		return true;
	}
	
	//
	// Updates a existing find
	// Routed from save() if 'id' is present in POST data
	//
	function edit(){
		
		global $_POST, $dblink;
		
		// get id to update
		$id = cleanup($_POST['id']);
		
		// fetch the fields
		$result = mysql_query("SELECT * FROM $this->findtable LIMIT 0,1");
		
		// get column metadata
		$i = 0;
		while ($i < mysql_num_fields($result)) {
		    $meta = mysql_fetch_field($result, $i);
		    if ($meta && $meta->name!='id' && isset($_POST[$meta->name])) $dt[$meta->name]=cleanup($_POST[$meta->name]);
		    $i++;
		}
		
		// set project specific data
		$dt['project_id']=$this->project_id;
		$dt['device_id']=$this->device_id;
		if (isset($dt['find_time'])) $dt['find_time'] = date("Y-m-d H:i:s",strtotime($dt['find_time']));
		
		// set extra data
		$dt['post_data'] = serialize($_POST);
		$dt['updated_at'] = date("Y-m-d H:i:s", time());		
		
		$query = "UPDATE $this->findtable SET ";
		foreach ($dt as $k=>$v){
			$query .= " `$k`='$v', ";
		}

		$query .= " revision=revision+1 WHERE id='$id'";

		$result = mysql_query($query);
		
		if ($result) {
			$this->set_find_status($id);
		}
		else{			
			$this->set_error("DB Write error: ".mysql_error($dblink));
			return false;
		}

		return true;
		
	}
	
	//
	// Get id(s) (id, revision)
	// 
	function get_ids($id=''){
		
		global $dblink;
		
		if ($id=='') $query = "SELECT * FROM $this->findtable";
		else $query = "SELECT * FROM $this->findtable WHERE id='$id'";
		
		$result = mysql_query($query);

		if ($result){
			$arr = array();
			while ($row = mysql_fetch_assoc($result)){
				
				$r['id']=$row['id'];
				$r['revision']=$row['revision'];

				$arr[] = $r;
			}
			$this->result['find_ids'] = $arr;
		}
		else{
			
		}			
		
	}
	
	//
	// Get find row(s)
	//
	function get_finds($id=''){
		
		global $dblink;
		$id = cleanup($_POST['id']);
		if ($id=='') $query = "SELECT * FROM $this->findtable";
		else $query = "SELECT * FROM $this->findtable WHERE id='$id'";
		
		$result = mysql_query($query);

		if ($result){
			$arr = array();
			while ($row = mysql_fetch_assoc($result)){
				
				// remove data we dont want to report
				unset($row["post_data"]);
				unset($row["device_id"]); 
				unset($row["project_id"]);
				unset($row["created_at"]);
				unset($row["updated_at"]);
				
				foreach ($row as $k => $v) {
					if (preg_match("/id$/", $k)){ //if the text ends with id, it is an integer most likely
						$row[$k] = intval($v);
					}else if (preg_match("/(latitude|longitude)/", $k)){
						$row[$k] = doubleval($v);
					}
				}
				$arr[] = $row;
			}
			$this->result['finds'] = $arr;
		}
		else{
			
		}		
		
	}
	
	//
	// checks if app_key and imei are valid (also sets $project_id and $device_id if so)
	//
	function is_valid_request(){
		
		global $_POST, $dblink;
		
		$appkey = cleanup($_POST['app_key']);
		$imei = cleanup($_POST['imei']);
		
		// check if app key is valid and get project id for it
		if ($appkey=='' || strlen($appkey)!=32) {
			$this->set_error("Invalid Application Key");
			return false;
		}
		
		$result = mysql_query("SELECT * FROM projects WHERE app_key='$appkey'");
		if (mysql_num_rows($result)>0){
			$row = mysql_fetch_assoc($result);
			$this->project_id = $row['id'];
			
			$this->findtable = 'finds_'.$row['id'].'_'.preg_replace('/[^a-z0-9_]/','',str_replace(" ","_",strtolower($row['name'])));
		}
		else{
			$this->set_error("Application Key doesn't exist");
		}
		
		// now check if imei is registered
		$result = mysql_query("SELECT * FROM devices WHERE project_id='$this->project_id' AND imei='$imei'");
		if (mysql_num_rows($result)>0){
			$row = mysql_fetch_assoc($result);
			$this->device_id = $row['id'];
		}
		else{
			$this->set_error("Device not registered");
			return false;
		}
		
		// check if table exists
		$result = mysql_query("SELECT * FROM $this->findtable LIMIT 0,1");
		if (!$result) {
			$this->set_error('Table not found');
			return false;
		}
		
		return true;	
		
	}
	
	//
	// Sets status message
	// To be called if status is something else than 'OK'
	//
	function set_status($msg){
		$this->result['status'] = $msg;		
	}
	
	//
	// Sets error message
	//
	function set_error($msg){
		$this->result['error'] = $msg;
		if ($this->result['status'] == 'OK') 
			$this->result['status'] = '';	// do not change if any other status msg than 'OK' set
	}
	
	//
	// sets find 'id' and 'revision' to result-set
	//
	function set_find_status($id){
		
		global $dblink;
		
		$result = mysql_query("SELECT * FROM $this->findtable WHERE id='$id'");
		$row = mysql_fetch_assoc($result);
		
		$this->result['id']=$row['id'];
		$this->result['revision']=$row['revision'];
	
	}
	
	//
	// Outputs result-set (id, revision, status, error, ...) in JSON format
	//
	function report(){
		return json_encode($this->result);
	}
	
}


/*

Posit_communication v1.0

class posit_communication{
	function posit_communication(){
		$this->arr = array();
		$this->arr['status'] = 'OK';
	}
	function set_finds($arrs,$status='OK'){
		//remove post_data item
		$this->arr = $arrs;
		$this->set_status($status);
	}
	function set_status($message){
		$this->arr['status'] = $message;
	}
	function get_last_find_id($rev=1){
		$this->arr['id'] = mysql_insert_id();
		$this->arr['revision'] = $rev;
		return $this->report();
	}
	function set_error($message){
		$this->set_status($message);
	}
	function report(){
		return json_encode($this->arr);
	}
	function reset(){
		$this->posit_communication();
	}
}
*/
?>