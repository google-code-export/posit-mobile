<?php

class CustomPosit  {
	function __construct($data){
		$this->name = $data[0];
		$this->project_id = $data[1];
		$this->description= $data[2];
		$this->sync_on = $data[3];
		$this->device_id = $data[4];

	}
	
	function build(){
		

		$this->makeDirs();
		$ret = $this->compilePosit();
		$this->saveToDatabase($ret);
	}
	function putCustomization(){
		global $smarty;
		$smarty->assign("name", $this->name);
		$smarty->assign("project_id", $this->project_id);
		$smarty->assign("project_name", "demo project");
		$smarty->assign("sync_on", $this->sync_on);
		$smarty->assign("auth_key", 0);
		$output = $smarty->fetch("customization/settings.json.tpl");
		$fh = fopen(POSIT_ANDROID_SRC_DIR."/assets/settings.json",'w');
		fwrite($fh, $output);
		fclose($fh);
		echo "Added customization";
			
	}
	
	function compilePosit(){
		global $dao;
		$this->putCustomization();
		$data = array();
		//$command = EXEC_DIR."/compilePosit ".POSIT_ANDROID_SRC_DIR." ".$this->project_id.
		//"  ".DOWNLOADS_DIR;
		$command = EXEC_DIR."/compilePosit ". $this->project_id;
//		print $command;
		exec($command,$data,$ret);
		echo $command;
		echo "<pre>";
		#if ($ret == 0) {                // check status code. if successful
			    foreach ($data as $line) {  // process array line by line
					        echo "$line \n";
							    }
		#} else {
		#	    echo "Error in command";    // if unsuccessful display error
		#}
		echo "</pre>";
		echo "<a href=\"web/projects\"> Go back to projects</a>";
		return $ret;

	
	}

	function download(){
	$file = DOWNLOADS_DIR."/projects/".$this->project_id."/download/PositMain-debug.apk";
	if(!file_exists($file))
	{
	    die('Error: File not found.');
	}else
	{
	    // Set headers
	    header("Cache-Control: public");
	    header("Content-Description: File Transfer");
	    header("Content-Disposition: attachment; filename=posit-".$this->project_id.".apk");
	    header("Content-Type: application/zip");
	    header("Content-Transfer-Encoding: binary");
	    readfile($file);
	}	
	}

	function saveToDatabase($ret) {
		global $dao;
		$this->auth_key=0;
		if ($ret == 0){
			$dao->addInstance($this->project_id, $this->name, $this->description, $this->sync_on, $this->auth_key);
		}
	
	}

	function makeDirs(){
		$project_dir= DOWNLOADS_DIR."/projects/".$this->project_id;
		if (!file_exists($project_dir)){
			mkdir($project_dir, 0775);
			mkdir ($project_dir."/download", 0775);
			mkdir ($project_dir."/logs", 0775);
		}
	}
}
?>
