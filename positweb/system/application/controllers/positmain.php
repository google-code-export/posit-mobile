<?php


class POSITMain extends Controller  {
	
	function POSITMain(){
		parent::Controller();
		
		$this->load->library('DX_Auth'); 
		
	}
	
	function index(){
		
		
		
		if ($this->dx_auth->is_logged_in()){
			//$str= "Hello, "+ $this->dx_auth->get_username() + " | ";
			$str = "<a href='auth/logout/'>Logout</a>";
		}
		else{
			$str = "<a href='auth/login/'>Login / Register</a>";
		}
		
		$data['logininfo'] = $str;
		
		$this->load->view("positmain_view", $data);
	}
	
	
}

?>