<?php

function webController($path, $request) {
	global $smarty, $dao;
	list($reqPath, $queryString) = explode('?', $path);
	$pathParts = explode('/', substr($reqPath,1));
	list($action) = $pathParts;
	
	session_start();
	$authnStatus = checkAuthn(); 
	
	if(isLoggedIn()) {
		$smarty->assign("loggedIn", true);
		$smarty->assign("loginEmail", $_SESSION["loginEmail"]);
	} else {
		$smarty->assign("loggedIn", false);
	}
	
	if(checkAuthz($action) && $authnStatus != AUTHN_FAILED) {
		// user is authorized
	
	$loginId = $_SESSION["loginId"];
	if($loginId) {
		$loginUser = $dao->getUser($loginId);
		if($loginUser["privileges"] == "admin")
			$loginHasAdmin = true;
		else
			$loginHasAdmin = false;
	} else
		$loginhasAdmin = false;
		
		$smarty->assign("loginHasAdmin", $loginHasAdmin);
		
		switch($action) {
			case 'main':
				$smarty->display('main.tpl');
				break;
			case 'login':
				$smarty->display('header.tpl');
				$smarty->display('login.tpl');
				$smarty->display('footer.tpl');
				break;
			case 'login.do':
				if($_POST["loginDestination"])
					$destination = "../web". $_POST["loginDestination"];
				else
					$destination = "main";
				header("Location: $destination");
				break;
			case 'logout':
				session_destroy();
				header("Location: main");
				break;
			case 'register':
				$smarty->display('register.tpl');
				break;
			case 'register.do':
				extract($request);
				if(!$email)
					errorMessage("Email address is required.");
				if(!$firstName)
					errorMessage("First name is required.");
				if(!$lastName)
					errorMessage("Last name is required.");
				if(strlen($pass1) < 6)
					errorMessage("Password must be 6 characters or longer.");
				if($pass1 != $pass2)
					errorMessage("Password and password confirmation do not match.");
				
				$newUser = array($email, $firstName, $lastName, $pass1);
				$result = $dao->registerUser($newUser);

				if($result === REGISTRATION_EMAILEXISTS)
					errorMessage("That email address already exists.");
				$_SESSION["loggedIn"] = true;
				$_SESSION["loginEmail"] = $email;
				header("Location: main");
				break;
			case 'projects':
				$projects = $dao->getProjects();
				$smarty->assign("projects", $projects);
				$smarty->display("projects.tpl");
				break;
			case 'project.display':
				list($queryType, $queryValue) = explode("=", $queryString);
				$id = $queryValue;
				$project = $dao->getProject($id);
				$smarty->assign("project", $project);
				
				$finds = $dao->getFinds($id);
				$smarty->assign("finds", $finds);
				$smarty->display("project_display.tpl");
				break;
			case 'find.display':
				list($queryType, $queryValue) = explode("=", $queryString);
				$id = $queryValue;
				$find = $dao->getFind($id);
				$project_id = $find["project_id"];
				$project = $dao->getProject($project_id);;
				
				
				$smarty->assign("images",$find["images"]);
				$smarty->assign("project", $project);
				$smarty->assign("find", $find);
				$smarty->display("find_display.tpl");
				break;				
			case 'settings':
				$userId = $_SESSION["loginId"];
				$devices = $dao->getDevicesByUser($userId);
				
				$smarty->assign("devices", $devices);
				$smarty->display("settings/settings.tpl");
				break;
			case 'settings.registerDevice':
				$authKey = genAuthKey();
				$userId = $_SESSION["loginId"];
				
				$result = $dao->registerDevicePending($userId, $authKey);
				
				$server = SERVER_BASE_URI;

				$email = $_SESSION["loginEmail"];
				
				$data = array(
					"server" => $server,
					"authKey" => $authKey
				);
				
				$smarty->assign("baseUrl", $server);
				$smarty->assign("authKey", $authKey);
				$smarty->assign("data", urlencode(json_encode($data)));
				$smarty->display("settings/registerDevice.tpl");
				break;
			case 'settings.registerDevice.do':
				$imei = $request["imei"];
				$deviceName = $request["deviceName"];
				
				$result = $dao->changeDeviceNickname($imei, $deviceName);
				
				header("Location: settings");
				break;
			
			case 'removeDevice.do': 
				$imei = $request["imei"];
				$dao->removeDevice($imei);
				header("Location: settings");
				break;
			
			case 'purge':
				echo "Purging pending device registrations...";
				$dao->purgePendingDevices();
				break;
			
			case 'displayPicture':
				$id=$request["id"];
				$image=$dao->getPicture($id);
				$size=$request["size"];
				if ($size == "thumb"){
					$data=$image["data_thumb"];
				}
				else{
					$data=$image["data_full"];
				}
				$mimeType=$image["mime_type"];
				header("Content-Type: $mimeType");
				echo $data;
				break;
	
			
			case 'admin':
				$smarty->display("admin/admin.tpl");
				break;	
			
			default:
				header("Location: main");
		}
	
	} else if($authnStatus == AUTHN_OK) {
		// user is authenticated but not authorized
	} else if($authnStatus == AUTHN_FAILED){
		// user failed authentication'
		$smarty->display("header.tpl");
		$smarty->assign("errMsg", "Login failed.");
		$smarty->display("login.tpl");
		$smarty->display("footer.tpl");
	} else {
		// user is not authenticated
		$smarty->display("header.tpl");
		$smarty->assign("destination", $path);
		$smarty->display("login.tpl");
		$smarty->display("footer.tpl");
	}
}

?>