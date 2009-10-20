<?php

/*
	
	POSIT Web
	@author: Jwalanta Shrestha
	
	Save Finds data to DB
	Get Finds data from DB
	added by prasanna:
	@done the verification code can go in one function
	@done the results should be reported back to the phone in json format?
*/

require_once("includes/bootstrap.php");

//for posit communication
$posit_comm = new Posit_communication();

if ($posit_comm->is_valid_request()){
	
	if (isset($_GET['id'])) $id=cleanup($_GET['id']); else $id='';

	switch ($_GET['q']){
		case 'save':
			$posit_comm->save();
			break;
			
		case 'get_finds':
			$posit_comm->get_finds($id);
			break;
			
		case 'get_ids':
			$posit_comm->get_ids($id);
			break;
			
		case 'edit':
			$posit_comm->edit($id);
			break;
	}
	
}

echo $posit_comm->report();

?>