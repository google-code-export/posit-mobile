<?php


function apiController($path, $request, $files = null) {

	global $dao;
	list($reqPath, $queryString) = explode('?', $path);
	$pathParts = explode('/', substr($reqPath,1));
	list($action) = $pathParts;
	
	$authKey = $request["authKey"];
	
	if($action != "registerDevice" && $action != "getPendingDeviceStatus" && !$authKey) {
		$response = array(
			"errorCode" => ERR_AUTHKEY_MISSING,
			"errorMessage" => "You must provide an authentication key with each request."
		);
		echo json_encode($response);
		die();
	}
	
	$device = $dao->getDeviceByAuthKey($authKey);
	
	if($action != "registerDevice" && $action != "getPendingDeviceStatus" && !$device) {
		$response = array(
			"errorCode" => ERR_AUTHKEY_INVALID,
			"errorMessage" => "Invalid authentication key."
		);
		echo json_encode($response);
		die();
	}
	
	$deviceUserId = $device["user_id"];
	$deviceIdentifier = $device["imei"];
	
	switch($action) {
		case 'registerDevice':
			$imei = $request["imei"];
			$name = null;
			$result = $dao->confirmDevice($authKey, $imei, $name);
			echo json_encode($result);
			break;
		case 'getPendingDeviceStatus':
			$device = $dao->getDeviceByAuthKey($authKey);
			if($device["status"] == "ok")
				echo json_encode($device);
			else
				echo json_encode(false);
			break;
		case 'listOpenProjects':
			$result = $dao->getProjects(PROJECTS_OPEN);
			echo json_encode($result);
			break;
		case 'listMyProjects':
			$result = $dao->getUserProjects($deviceUserId);
			echo json_encode($result);
			break;
		case 'listFinds':
			echo json_encode($dao->getFinds($request["projectId"]));
			break;
		case 'getFind':
			$result = $dao->getFind($request["id"]);
			echo json_encode($result);
			break;
		case 'deleteFind':
			echo $dao->deleteFind($request["id"]);
			break;
		case 'deleteProject':
			$dao->deleteProject($resquest["projectId"]);
			break;
		case 'deleteAllFinds':
			$dao->deleteAllFinds($request["projectId"]);
			break;
		case 'createFind':
			echo $dao->createFind($request["id"], $request["projectId"], 
				$request["name"], $request["description"], $request["latitude"], $request["longitude"], $request["revision"]);
			break;
		case 'updateFind':
			echo $dao->updateFind($request["id"],$request["name"], $request["description"], $request["revision"]);
			break;
		case 'attachPicture':
			$imagedata=base64_decode($request["dataFull"]);
			$imagethumbdata=base64_decode($request["dataThumb"]);
			$result=$dao->addPictureToFind($request["id"], $request["findId"], $request["mimeType"], $imagedata, $imagethumbdata);
			echo json_encode($result);
			break;
		case 'removePicture':
			$dao->deletePictureFromFind($request["id"]);
			break;
		case 'deleteAllPictures':
			$dao->deleteImages($request["findId"]);
			break;
		case 'getPicture':
			$picture=$dao->getPicture($request["id"]);
			$imageEncoded=base64_encode($picture["data_full"]);
			$imageThumbEncoded=base64_encode($picture["data_thumb"]);
			$pictureEncoded=$picture;
			if ($imageEncoded != "")
				$pictureEncoded["data_full"]=$imageEncoded;
			if ($imageThumbEncoded != "")
				$pictureEncoded["data_thumb"]=$imageThumbEncoded;
			if (count($pictureEncoded) > 0)
				echo json_encode($pictureEncoded);
			else echo "false";
			break;
		case 'getPicturesByFind' :
			$pictures = $dao->getPicturesByFind($request["findId"]);
			$result = array();
			foreach($pictures as $pic) {
				$imageEncoded=base64_encode($pic["data_full"]);
				$imageThumbEncoded=base64_encode($pic["data_thumb"]);
				$pictureEncoded=$pic;
				if ($imageEncoded != "")
					$pictureEncoded["data_full"]=$imageEncoded;
				if ($imageThumbEncoded != "")
					$pictureEncoded["data_thumb"]=$imageThumbEncoded;
				if (count($pictureEncoded) > 0)
					$result[] = $pictureEncoded;
			}
			if(count($result) > 0)
				echo json_encode($result);
			else echo "false";
			break;
		case 'searchFinds':
 			$search_value=$request['search_value'];
			$project_id=$request['project_id'];
     			$result=$dao->searchFinds($search_value, $project_id);
 			echo json_encode($result);
           		break;
	        case 'execCommand':
			$command=$request['command'];
        		echo $dao->execCommand($command);
			break;	
        
		default:
			break;
	}
}

?>
