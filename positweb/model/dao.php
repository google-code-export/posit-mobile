<?php
/**
* Project:		positweb
* File name:	dao.php
* Description:	database access
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

function dbConnect() {
	$host 		= DB_HOST;
	$user 		= DB_USER;
	$pass 		= DB_PASS;
	$db_name 	= DB_NAME;
	
	try {
	    $db = new PDO("mysql:host=$host;dbname=$db_name", $user, $pass);
	} catch (PDOException $e) {
	    errorMessage("Database error: ". $e->getMessage());
	    die();
	}
	
	mysql_connect($host, $user, $pass);
	mysql_select_db($db_name);
	
	return $db;
}


class DAO {
	private $db;
	
	function DAO() {
		$this->db = dbConnect();
	}
	
	function getUser($userId) {
		$stmt = $this->db->prepare(
			"SELECT email, first_name, last_name, privileges, create_time FROM user WHERE id = :userId"
		);
		
		$stmt->bindValue(":userId", $userId);
		$stmt->execute();
		
		return $stmt->fetch(PDO::FETCH_ASSOC);
	}
	
	function newProject($name, $description) {
		$name = addslashes($name);
		$description = addslashes($description);
		/*$stmt = $this->db->prepare(
			"INSERT INTO project (name, description) VALUES (:name, :description)"
		); // or print_r($this->db->errorInfo()) && die();
		
		$stmt->bindValue(":name", $name);
		$stmt->bindValue(":description", $description);
		
		print_r($stmt);
		
		$stmt->execute();*/
		
		$stmt = $this->db->prepare("INSERT INTO project (name) VALUES ('$name')");
		$stmt->execute();
	}
	
	function getProjects($permissionType = PROJECTS_ALL) {
		if($permissionType == PROJECTS_OPEN)
			$whereClause = "where permission_type = 'open'";
		else if($permissionType == PROJECTS_CLOSED)
			$whereClause = "where permission_type = 'closed'";
		else
			$whereClause = "";
			
		$stmt = $this->db->prepare(
			"select id, name, create_time, permission_type
			 from project ". $whereClause
		);
		
		$stmt->execute();
		$result = $stmt->fetchAll(PDO::FETCH_ASSOC);

		return $result;
	}
	
	function getUserProjects($userId) {
		$stmt = $this->db->prepare(
			"select project_id from user_project 
			 where user_id = :userId"
		);
		
		$stmt->bindValue(":userId", $userId);
		$stmt->execute();
		$result = $stmt->fetchAll(PDO::FETCH_ASSOC);
		
		return $result;
	}
	
	function getFinds($projectId) {
		$stmt = $this->db->prepare("select id, name, description, add_time, modify_time,
			latitude, longitude, revision from find where project_id = :projectId"
		);
		$stmt->bindValue(":projectId", $projectId);
		$stmt->execute();
		$temp = $stmt->fetchAll(PDO::FETCH_ASSOC);
		$result = array();
		
		foreach ($temp as $find) {
			$stmt = $this->db->prepare("select id from photo where find_id = :id");
			$stmt->bindValue(":id", $find["id"]);
			$stmt->execute();
			$imageResult = $stmt->fetchAll(PDO::FETCH_ASSOC);
			
			$find["images"] = array();
			
			foreach($imageResult as $image) {
				$find["images"][] = $image["id"];
			}
			
			$stmt = $this->db->prepare("select id from video where find_id = :id");
			$stmt->bindValue(":id", $find["id"]);
			$stmt->execute();
			$videoResult = $stmt->fetchAll(PDO::FETCH_ASSOC);
			
			$find["videos"] = array();
			
			foreach($videoResult as $video) {
				$find["videos"][] = $video["id"];
			}
			
			$stmt = $this->db->prepare("select id from audio where find_id= :id");
			$stmt->bindValue(":id", $find["id"]);
			$stmt->execute();
			$audioResult = $stmt->fetchAll(PDO::FETCH_ASSOC);
			
			$find["audioClips"] = array();
			
			foreach($audioResult as $audio) {
				$find["audioClips"][] = $audio["id"];
			}
			
			$result[] = $find;
		}

		return $result;
	}
	
	function getFind($id) {
		$stmt = $this->db->prepare("select id, name, description, add_time, modify_time, 
			latitude, longitude, revision from find where id = :id");
		
		$stmt->bindValue(":id", $id);
		$stmt->execute();
		$temp = $stmt->fetchAll(PDO::FETCH_ASSOC);
		$result = array();
		$result[0]= $temp[0];
		$result[0]["images"] = array();
		
		$stmt = $this->db->prepare("select id from photo where find_id = :id");
		$stmt->bindValue(":id", $id);
		$stmt->execute();
		$imageResult = $stmt->fetchAll(PDO::FETCH_ASSOC);

		foreach($imageResult as $image) {
			$result[0]["images"][] = $image["id"];
		}
		
		$result[0]["audios"] = array();
		$stmt = $this->db->prepare("select id from audio where find_id = :id");
		$stmt->bindValue(":id", $id);
		$stmt->execute();
		$audioResult = $stmt->fetchAll(PDO::FETCH_ASSOC);

		foreach($audioResult as $audio) {
			$result[0]["audios"][] = $audio["id"];
		}

		$result[0]["videos"] = array();
		$stmt = $this->db->prepare("select id from video where find_id = :id");
		$stmt->bindValue(":id", $id);
		$stmt->execute();
		$videoResult = $stmt->fetchAll(PDO::FETCH_ASSOC);

		foreach($videoResult as $video) {
			$result[0]["videos"][] = $video["id"];
		}

		return $result[0];
	
	}

	function getProject($id) {
		$stmt = $this->db->prepare(
			"select name, create_time, permission_type
			 from project where id = :id");
		
		$stmt->bindValue(":id", $id);	
		$stmt->execute();
		$result = $stmt->fetch(PDO::FETCH_ASSOC);
		
		return $result;
	}
	
	function getProjectUsers($projectId) {
		$stmt = $this->db->prepare(
			"select user_id from user_project 
			 where project_id = :projectId"
		);
		
		$stmt->bindValue(":projectId", $projectId);
		$stmt->execute();
		$result = $stmt->fetch(PDO::FETCH_ASSOC);
		
		return $result;
	}
	
	function getPicture($pictureId){
		$stmt = $this->db->prepare(
			"select id,find_id,mime_type,data_full,data_thumb from photo
			where id = :id"
			
		);
		$stmt->bindValue(':id', $pictureId);
		$stmt->execute();
		return $stmt->fetch(PDO::FETCH_ASSOC);
	}
	
	function getPicturesByFind($findId){
		$stmt = $this->db->prepare(
			"select id,find_id,mime_type,data_full,data_thumb from photo
			where find_id = :findId"
			
		);
		$stmt->bindValue(":findId", $findId);
		$stmt->execute();
		$temp = $stmt->fetchAll(PDO::FETCH_ASSOC);
		$result = array();
		foreach ($temp as $picture) {
			$result[] = $picture;
		}
		return $result;
	}
	
	function getVideo($videoId) {
		$stmt = $this->db->prepare("select id, find_id, mime_type, data_path from video where id=:id");
		$stmt->bindValue(':id', $videoId);
		$stmt->execute();
		return $stmt->fetch(PDO::FETCH_ASSOC);
	}
	
	function getAudioClip($audioId) {
		$stmt = $this->db->prepare("select id, find_id, mime_type, data_path from audio where id=:id");
		$stmt->bindValue(':id', $audioId);
		$stmt->execute();
		return $stmt->fetch(PDO::FETCH_ASSOC);
	}
	
	function checkLogin($email, $pass) {
		$stmt = $this->db->prepare(
			"SELECT id, first_name, last_name
			 FROM user
			 WHERE email = :email AND password = SHA1(:pass)"
		) or print_r($this->db->errorInfo()) && die();
		$stmt->bindValue(':email', $email);
		$stmt->bindValue(':pass', $pass);
		
		$stmt->execute();
		
		if($result = $stmt->fetch(PDO::FETCH_ASSOC))
			return $result;
		else
			return false;
	}
	
	function deleteFind($findId) {
		$stmt = $this->db->prepare("delete from find where id = :findId");
		$stmt->bindvalue(":findId", $findId);
		$stmt->execute();
		$this->deleteImages($findId);
		echo "Deletion of find with id = ".$findId." successful.";

	}
	
	function deleteAllFinds($projectId) {
		$stmt = $this->db->prepare("delete from find where project_id = :projectId");
		$stmt->bindvalue(":projectId", $projectId);
		$stmt->execute();
	}
	
	function deleteProject($id) {
		$stmt = $this->db->prepare("delete from project where id = :id");
		$stmt->bindValue(":id", $id);
		$stmt->execute();
	}
	
	function deleteImages($findId) {
		$stmt = $this->db->prepare("delete from photo where find_id = :findId");
		$stmt->bindValue(":findId", $findId);
		$stmt->execute();
		echo "Deletion of image with find_id = ".$findId." successful.";
	}
	
	function deleteVideos($findId) {
		$stmt = $this->db->prepare("delete from video where find_id = :findId");
		$stmt->bindValue(":findId", $findId);
		$stmt->execute();
		echo "Deletion of video with find_id = ".$findId." successful.";
	}
	
	function deleteAudioClips($findId) {
		$stmt = $this->db->prepare("delete from audio where find_id = :findId");
		$stmt->bindValue(":findId", $findId);
		$stmt->execute();
		echo "Deletion of audio clip with find_id = ".$findId." successful.";
	}
	
	function createFind($id, $projectId, $name, $description, $latitude, $longitude, $revision) {
		$stmt = $this->db->prepare(
			"insert into find (id, project_id, name, description, 
			latitude, longitude, add_time, modify_time, revision) VALUES
			(:id, :projectId, :name, :description, :latitude, :longitude ,now(), now(), :revision)"
		);
		
		$stmt->bindValue(":id", $id);
		$stmt->bindValue(":projectId", $projectId);
		$stmt->bindValue(":name", $name);
		$stmt->bindValue(":description", $description);
		$stmt->bindValue(":latitude", $latitude);
		$stmt->bindValue(":longitude", $longitude);
		$stmt->bindValue(":revision", $revision);
			
		$stmt->execute();
		return "Inserted into database";
	}
	
	function updateFind($id, $name, $description, $revision) {
		$stmt = $this->db->prepare("update find set 
			name = :name, description = :description, 
			revision = :revision where id = :id");
		
		$stmt->bindValue(":name", $name);
		$stmt->bindValue(":description", $description);
		$stmt->bindValue(":revision", $revision);
		$stmt->bindValue(":id", $id);
		
		$stmt->execute();
		return "Inserted into database";
	}
	
	function addPictureToFind($id, $findId, $mimeType, $dataFull, $dataThumb) {
		$stmt = $this->db->prepare(
			"insert into photo (id, find_id, mime_type, data_full, data_thumb)
			VALUES (:id, :findId, :mimeType, :dataFull, :dataThumb)"
		);
		$stmt->bindValue(":id",$id);
		$stmt->bindValue(":findId",$findId);
		$stmt->bindValue(":mimeType",$mimeType);
		$stmt->bindValue(":dataFull",$dataFull);
		$stmt->bindValue(":dataThumb",$dataThumb);
		
		$stmt->execute();
		return $stmt->fetch(PDO::FETCH_ASSOC);
	}
	
	function addVideoToFind($id, $findId, $mimeType, $dataPath) {
		$stmt = $this->db->prepare(
			"insert into video (id, find_id, mime_type, data_path)
			VALUES (:id, :findId, :mimeType, :dataPath)"
		);
		$stmt->bindValue(":id", $id);
		$stmt->bindValue(":findId", $findId);
		$stmt->bindValue(":mimeType", $mimeType);
		$stmt->bindValue(":dataPath", $dataPath);
		
		$stmt->execute();
		return $stmt->fetch(PDO::FETCH_ASSOC);
	}
	
	function addAudioClipToFind($id, $findId, $mimeType, $dataPath) {
		$stmt = $this->db->prepare(
			"insert into audio (id, find_id, mime_type, data_path)
			VALUES (:id, :findId, :mimeType, :dataPath)"
		);
		$stmt->bindValue(":id", $id);
		$stmt->bindValue(":findId", $findId);
		$stmt->bindValue(":mimeType", $mimeType);
		$stmt->bindValue(":dataPath", $dataPath);
		
		$stmt->execute();
		return $stmt->fetch(PDO::FETCH_ASSOC);
	}
	
	function deletePictureFromFind($id) {
		$stmt = $this->db->prepare(
			"delete from photo where id = :id"
		);
		$stmt->bindValue(":id", $id);
		$stmt.execute();
	}
	
	function deleteVideoFromFind($id) {
		$stmt = $this->db->prepare(
			"select data_path from video where id = :id");
		$stmt->bindValue(":id", $id);
		$stmt.execute();
		$video = $stmt->fetch(PDO::FETCH_ASSOC);
		$video_path = $video['data_path'];
		$fh = fopen("uploads/$video_path", 'w') or die("can't open file");
		fclose($fh);
		unlink($video_path);
		$stmt = $this->db->prepare(
			"delete from video where id = :id"
		);
		$stmt->bindValue(":id", $id);
		$stmt.execute();
	}
	
	function deleteAudioClipFromFind($id) {+
		$stmt = $this->db->prepare(
			"select data_path from audio where id = :id");
		$stmt->bindValue(":id", $id);
		$stmt.execute();
		$audio = $stmt->fetch(PDO::FETCH_ASSOC);
		$audio_path = $audio['data_path'];
		$fh = fopen("uploads/$audio_path", 'w') or die("can't open file");
		fclose($fh);
		unlink($audio_path);
		$stmt = $this->db->prepare(
			"delete from audio where id = :id"
		);
		$stmt->bindValue(":id", $id);
		$stmt.execute();
	}
	
	function registerUser($newUser) {
		list($email, $firstName, $lastName, $password) = $newUser;
		
		$stmt = $this->db->prepare(
			"SELECT id FROM user WHERE email = :email"
		);
		
		$stmt->bindValue(":email", $email);
		$stmt->execute();
		
		if($stmt->fetch())
			return REGISTRATION_EMAILEXISTS;
		
		$stmt = $this->db->prepare(
			"INSERT INTO user (first_name, last_name, email, password, create_time)
			 VALUES (:firstName, :lastName, :email, SHA1(:password), now())"
		);
		$stmt->bindValue(":firstName", $firstName);
		$stmt->bindValue(":lastName", $lastName);
		$stmt->bindValue(":email", $email);
		$stmt->bindValue(":password", $password);
		$stmt->execute();
		return true;
	}
	
	function getDevicesByUser($userId) {
		$stmt = $this->db->prepare(
			"SELECT imei, name, auth_key, add_time
			 FROM device
			 WHERE user_id = :userId
			 AND status = 'ok'"
		);
		$stmt->bindValue(":userId", $userId);
		$stmt->execute();
		return $stmt->fetchAll(PDO::FETCH_ASSOC);
	}
	
	function getDeviceByAuthKey($authKey) {
		$stmt = $this->db->prepare(
			"SELECT imei, name, user_id, add_time, status
			 FROM device
			 WHERE auth_key = :authKey
			 LIMIT 1"
		); $stmt->bindValue(":authKey", $authKey);
		$stmt->execute();
		return $stmt->fetch(PDO::FETCH_ASSOC);
	}
	
	function registerDevicePending($userId, $authKey) {
		if(!$userId || !$authKey) return false;
		
		$stmt = $this->db->prepare(
			"INSERT INTO device (user_id, auth_key, add_time)
			 VALUES (:userId, :authKey, now())"
		);
		$stmt->bindValue(":userId", $userId);
		$stmt->bindValue(":authKey", $authKey);
		$stmt->execute();
		
		return true;
	}
	
	function confirmDevice($authKey, $imei, $name) {
		
		$stmt = $this->db->prepare(
			"SELECT auth_key FROM device WHERE imei = :imei"
		);
		$stmt->bindValue(":imei", $imei);
		$stmt->execute();
		
		if($existingDevice = $stmt->fetch(PDO::FETCH_ASSOC)) {
			$res = mysql_query("select user_id from device where auth_key = '$authKey'") or die(mysql_error());
			list($userId) = mysql_fetch_array($res, MYSQL_NUM);
			
			mysql_query(
				"DELETE FROM device WHERE auth_key = '$authKey'"
			);
			mysql_query(
				"UPDATE device SET auth_key = '$authKey', status = 'ok', user_id = '$userId' WHERE imei = '$imei'"
			);
			
			return true;
			//$stmt->bindValue(":authKey", $authKey);
			//$stmt->bindValue(":imei", $imei);
			//return $stmt->execute();
			/*
			$stmt = $this->db->prepare(
				"SELECT name FROM device WHERE imei = :imei"
			);
			$stmt->bindValue(":imei", $imei);
			$stmt->execute();
			
			list($name) = $stmt->fetch(PDO::FETCH_ASSOC);
			*/
		}
		
		$stmt = $this->db->prepare(
			"UPDATE device SET
			 imei = :imei,
			 name = :name,
			 status = 'ok'
			 WHERE auth_key = :authKey"
		);
		$stmt->bindValue(":imei", $imei);
		$stmt->bindValue(":name", $name);
		$stmt->bindValue(":authKey", $authKey);
		$result = $stmt->execute();
		return $result;
	}
	
	function addSandboxDevice($authKey, $imei) {
		
		$stmt = $this->db->prepare("delete from device where imei = :imei");
		$stmt->bindValue(":imei", $imei);
		$stmt->execute();
		
		$stmt = $this->db->prepare(
			"INSERT INTO device (imei, user_id, auth_key, add_time, status)
			 VALUES (:imei, 0, :authKey, now(), 'ok')"
		);
		$stmt->bindValue(":imei", $imei);
		$stmt->bindValue(":authKey", $authKey);
		$stmt->execute();
		return true;
	}
	
	function changeDeviceNickname($imei, $name) {
		$stmt = $this->db->prepare(
			"UPDATE device SET name = :name WHERE imei = :imei"
		);
		$stmt->bindValue(":name", $name);
		$stmt->bindValue(":imei", $imei);
		return $stmt->execute();
	}
	
	function removeDevice($imei) {
		$stmt = $this->db->prepare(
			"DELETE FROM device WHERE imei = :imei"
		);
		$stmt->bindValue(":imei", $imei);
		return $stmt->execute();
	}
	
	function purgePendingDevices() {
		$stmt = $this->db->prepare("DELETE FROM device WHERE imei IS NULL");
		$stmt->execute();
	}

	function searchFinds($search_value, $project_id){
		$stmt = $this->db->prepare(
			"SELECT id, name, description
			FROM find
			WHERE project_id = :project_id AND name LIKE CONCAT('%', :search_value, '%')"
			) or print_r($this->db->errorInfo()) && die();

		$stmt->bindValue(":search_value", $search_value);
		$stmt->bindValue(":project_id", $project_id);
		$stmt->execute();
		$available_values = array();
		$temp = $stmt->fetchAll(PDO::FETCH_ASSOC);
		
		foreach($temp as $value){
			$available_values[] = $value;
		}
		return $available_values;
	}

	function execCommand($command_value){
		if ($command_value == "create_sample_text") {
			$file = "test/HelloWorld.txt";
			$handler = fopen($file, 'w') or die("can't open file");
			$data = "This is a sample text created in order to demonstrate the functionality of the command line";
			fwrite($handler, $data);
			fclose($handler);
		}
	}

}

?>
