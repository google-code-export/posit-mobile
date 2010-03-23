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

/**
 * 
 * The class for accessing the database
 *	
 */
class DAO {
	private $db;
	
	function DAO() {
		$this->db = dbConnect();
	}
	
	/**
	 *  returns a comma-delimited list of all finds since last sync for a given device
	 *  @param unknown_type $imei the device's id
	 */
	 function getDeltaFindsIds($imei, $pid) {
	 	Log::getInstance()->log("getDeltaFindsIds: $imei");
		
		// Get the timestamp of the last sync with this device
		$stmt = $this->db->prepare(
			"SELECT MAX(sync_history.time) FROM sync_history WHERE imei = :imei"); 
		$stmt->bindValue(":imei", $imei);
		$stmt->execute();
		$result = $stmt->fetch(PDO::FETCH_NUM);
		$time = $result[0];
		Log::getInstance()->log("getDeltaFindsIds: Max Time = |$time|");
                
                //  If there is no MAX time, this is a new device, so get all Finds from other phones

//		if (strcmp($time,"") == 0) {
		if ($time == NULL) {
			Log::getInstance()->log("getDeltaFindsIds: IF time = $time");	
//		        $res = mysql_query("SELECT DISTINCT find_guid FROM find_history WHERE imei != '$imei'") or die(mysql_error());  
		        $res = mysql_query(
			"SELECT DISTINCT find_history.find_guid FROM find_history,find WHERE find_history.imei != '$imei' AND find.barcode_id=find_history.find_guid AND find.project_id = '$pid'") 
			or die(mysql_error());  
                } else {
			Log::getInstance()->log("getDeltaFindsIds: ELSE time = $time");	
//       		        $res = mysql_query("SELECT DISTINCT find_guid FROM find_history WHERE TIMESTAMPDIFF(SECOND,'$time',time) > 0") or die(mysql_error());  
       		        $res = mysql_query(
			"SELECT DISTINCT find_history.find_guid FROM find_history,find WHERE TIMESTAMPDIFF(SECOND,'$time',time) > 0 AND find.barcode_id=find_history.find_guid AND find.project_id = '$pid'") 
			or die(mysql_error());  
                }

//SELECT DISTINCT find_history.find_guid FROM find_history, find WHERE find_history.imei != '351677030043731' AND find.barcode_id = find_history.find_guid AND find.project_id = 6


		// Get a list of the Finds (guids) that have changed since the last update

		while ($row = mysql_fetch_row($res)) {
			$list .= "$row[0],";
		}    				
		Log::getInstance()->log("getDeltaFindsIds: $list");
		return $list;
	 }
	
	/**
	 * records a record in the sync_history table
	 * @param unknown_type $imei
	 */
	 function recordSync($imei) {
		Log::getInstance()->log("recordSycn: $imei");
		$stmt = $this->db->prepare(
			"INSERT INTO sync_history (imei) VALUES (:imei)"
		); 
		$stmt->bindValue(":imei", $imei);
		$stmt->execute();
		$lastid = $this->db->lastInsertId();
		Log::getInstance()->log("lastInsertId()=$lastid");
		return $lastid;
	 }
	 
	/**
	 * get user from the user ID
	 * @param unknown_type $userId
	 */
	function getUser($userId) {
		Log::getInstance()->log("getUser: $userId");

		$stmt = $this->db->prepare(
			"SELECT email, first_name, last_name, privileges, create_time FROM user WHERE id = :userId"
		);
		
		$stmt->bindValue(":userId", $userId);
		$stmt->execute();
		
		return $stmt->fetch(PDO::FETCH_ASSOC);
	}
	/**
	 * creates a new project
	 * @param unknown_type $name
	 * @param unknown_type $description
	 */
	function newProject($name, $description) {
		Log::getInstance()->log("newProject: $name, $description");

		$name = addslashes($name);
		Log::getInstance()->log($description);
		$description = addslashes($description);
		$stmt = $this->db->prepare(
			"INSERT INTO project (name, description) VALUES (:name, :description)"
		); // or print_r($this->db->errorInfo()) && die();
		
		$stmt->bindValue(":name", $name);
		$stmt->bindValue(":description", $description);
		
		
		$stmt->execute();
		
		/*$stmt = $this->db->prepare("INSERT INTO project (name) VALUES ('$name')");*/
		
	}
	/**
	 * gets an associative array of all the projects that are accessible to the entity
	 * @param unknown_type $permissionType
	 */
	function getProjects($permissionType = PROJECTS_ALL) {
		Log::getInstance()->log("getProjects: $permissionType");

		if($permissionType == PROJECTS_OPEN)
			$whereClause = "where permission_type = 'open'";
		else if($permissionType == PROJECTS_CLOSED)
			$whereClause = "where permission_type = 'closed'";
		else
			$whereClause = "";
			
		$stmt = $this->db->prepare(
			"select id, name, description, create_time, permission_type
			 from project ". $whereClause
		);
//		$stmt->execute();
		$stmt->execute() or die("[Error MySQl dao.php line 154]" . mysql_error());
		$result = $stmt->fetchAll(PDO::FETCH_ASSOC);
		Log::getInstance()->log("getProjects: $result");

		return $result;
	}
	/**
	 * get the projects accessible to the user
	 * @param unknown_type $userId
	 */
	function getUserProjects($userId) {
	    Log::getInstance()->log("getUserProjects: $userId");

		$stmt = $this->db->prepare(
			"select project_id from user_project 
			 where user_id = :userId"
		);
		
		$stmt->bindValue(":userId", $userId);
		$stmt->execute();
		$result = $stmt->fetchAll(PDO::FETCH_ASSOC);
		
		return $result;
	}
	/**
	 * get all the finds for a project
	 * @param unknown_type $projectId
	 */
	function getFinds($projectId) {
		Log::getInstance()->log("getFinds: $projectId");

		$stmt = $this->db->prepare("select id, barcode_id, name, description, add_time, modify_time,
			latitude, longitude, revision from find where project_id = :projectId"
		);	
		$stmt->bindValue(":projectId", $projectId);
		$stmt->execute();
		$temp = $stmt->fetchAll(PDO::FETCH_ASSOC);
		$result = array();
		
		foreach ($temp as $find) {
//		echo "Find = " . $find["barcode_id"] . "<BR>";
//			$stmt = $this->db->prepare("select id from photo where find_id = :id");
			$stmt = $this->db->prepare("select imei, data_thumb from photo where guid = :id");
//			$stmt->bindValue(":id", $find["id"]);
			$stmt->bindValue(":id", $find["barcode_id"]);
			$stmt->execute();
			$imageResult = $stmt->fetchAll(PDO::FETCH_ASSOC);
			
			$find["blah"] = "blah blah blah";
			$find["images"] = array();
			
			foreach($imageResult as $image) {
				$img = "data:image/jpeg;base64," . base64_encode($image["data_thumb"]);
				$find["img"] = $img;
//                        echo "<img src=" . $img . ">";
//				$find["images"][] = $image["id"];
				$find["images"][] = $image["guid"];
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
	/**
	 * get a specific find
	 * @param unknown_type $id
	 */
	function getFind($guid) {
		Log::getInstance()->log("getFind: $guid");

//		$stmt = $this->db->prepare("select id, project_id, barcode_id, name, description, add_time, modify_time, 
//			latitude, longitude, revision from find where id = :id");
		$stmt = $this->db->prepare("select project_id, barcode_id, name, description, add_time, modify_time, 
			latitude, longitude, revision from find where barcode_id = :guid");		
		$stmt->bindValue(":guid", $guid);
		$stmt->execute();
		$temp = $stmt->fetchAll(PDO::FETCH_ASSOC);
		
/********* Try to return the thumbnail
		$stmt = $this->db->prepare("select imei, data_thumb from photo where guid = :id");
		$stmt->bindValue(":id", $guid);
		$stmt->execute();
		$imageResult = $stmt->fetchAll(PDO::FETCH_ASSOC);
			
		$find["images"] = array();
		foreach($imageResult as $image) {
			$img = "data:image/jpeg;base64," . base64_encode($image["data_thumb"]);
			$find["img"] = $img;
			$find["images"][] = $image["guid"];
		}
		
**************/
		
/*********
		foreach ($temp[0] as $key=>$value) {
			Log::getInstance()->log("getFind temp: $key = $value");
		}    				
***************/
		$result = array();
		$result[0]= $temp[0];
		Log::getInstance()->log("getFind length of record: " . count($result[0]));

		$result[0]["images"] = array();
		
//		$stmt = $this->db->prepare("select id from photo where find_id = :id");
//		$stmt->bindValue(":id", $id);
		$stmt = $this->db->prepare("select id from photo where guid = :id");
		$stmt->bindValue(":id", $guid);
		$stmt->execute();
		$imageResult = $stmt->fetchAll(PDO::FETCH_ASSOC);

		foreach($imageResult as $image) {
			$result[0]["images"][] = $image["id"];
		}
		
/************
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
*********************/
		return $result[0];
	
	}
	/**
	 * get a project object
	 * @param $id
	 */
	function getProject($id) {
		Log::getInstance()->log("getProject: $id");

		$stmt = $this->db->prepare(
			"select id, name, create_time, permission_type
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
	/**
	 * get a picture
	 * @param unknown_type $pictureId
	 */
	function getPicture($pictureId){
		Log::getInstance()->log("getPicture: $pictureId");

		$stmt = $this->db->prepare(
			"select id,guid,mime_type,data_full,data_thumb from photo
			where id = :id"
			
		);
//		print_r($this->db->errorInfo());
		$stmt->bindValue(':id', $pictureId);
		$stmt->execute();
		return $stmt->fetch(PDO::FETCH_ASSOC);
	}
	/**
	 * get all the pictures associated to a find
	 * @param unknown_type $findId
	 */
	function getPicturesByFind($guid){
		Log::getInstance()->log("getPicturesByFind: $guid");
		$stmt = $this->db->prepare(
			"select guid,mime_type,data_full,data_thumb, project_id, identifier from photo
			where guid = :guid"
			
		);
		$stmt->bindValue(":guid", $guid);
		$stmt->execute();
		$temp = $stmt->fetchAll(PDO::FETCH_ASSOC);
		$result = array();
		foreach ($temp as $picture) {
			$result[] = $picture;
		}
		return $result;
	}
	/**
	 * get the video by Id
	 * @param unknown_type $videoId
	 */
	function getVideo($videoId) {
		$stmt = $this->db->prepare("select id, find_id, mime_type, data_path from video where id=:id");
		$stmt->bindValue(':id', $videoId);
		$stmt->execute();
		return $stmt->fetch(PDO::FETCH_ASSOC);
	}
	/**
	 * get audio clip by Id
	 * @param unknown_type $audioId
	 */
	function getAudioClip($audioId) {
		$stmt = $this->db->prepare("select id, find_id, mime_type, data_path from audio where id=:id");
		$stmt->bindValue(':id', $audioId);
		$stmt->execute();
		return $stmt->fetch(PDO::FETCH_ASSOC);
	}
	/**
	 * verify the login based on email address and password entered
	 * @param $email
	 * @param $pass
	 */
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
	/**
	 * delete a find
	 * @param unknown_type $findId
	 */
	function deleteFind($findId) {
		Log::getInstance()->log("deleteFind: $findId");

		$stmt = $this->db->prepare("delete from find where barcode_id = :findId");
		$stmt->bindvalue(":findId", $findId);
		$stmt->execute();
		$this->deleteImages($findId);
		echo "Deletion of find with id = ".$findId." successful.";
		
		// Make an entry in find_history
		$stmt = $this->db->prepare(
			"insert into find_history (find_guid, action) VALUES
			(:find_guid, :action)"
		);
		$stmt->bindValue(":find_guid", $findId);
		$stmt->bindValue(":action", "delete");	
		$stmt->execute();
		
		$lastid = $this->db->lastInsertId();
		Log::getInstance()->log("Updated find_history, lastInsertId()=$lastid");
	}
	
	function addExpedition($projectId){
		$stmt = $this->db->prepare("INSERT INTO expedition ( project_id ) VALUES (:projectId)");
		$stmt->bindValue(":projectId", $projectId);
//		$stmt->bindValue(":userId", $userid);
		$stmt->execute();
		return $this->db->lastInsertId();
	}
	
	function addExpeditionPoint($expeditionId, $latitude, $longitude, $altitude, $swath){
		$stmt = $this->db->prepare("INSERT INTO gps_sample ( expedition_id, latitude, longitude , altitude, swath, sample_time)" 
		."VALUES (:expeditionId, :latitude, :longitude, :altitude, :swath, now() )");
		$stmt->bindValue(":expeditionId", $expeditionId);
		$stmt->bindValue(":latitude", $latitude);
		$stmt->bindValue(":longitude", $longitude);
		$stmt->bindValue(":altitude", $altitude);
		$stmt->bindValue(":swath", $swath);
		$stmt->execute();
		return $this->db->lastInsertId();
		//return $stmt->execute() > 0;
	}
	
	function getExpeditions($projectId){
		Log::getInstance()->log("getExpeditions: $projectId");
		$stmt = $this->db->prepare("SELECT id, name, description, project_id FROM expedition WHERE project_id= :projectId ");
		$stmt->bindValue(":projectId", $projectId);
		$stmt->execute();
		$temp = $stmt->fetchAll(PDO::FETCH_ASSOC);
		return $temp;
	}
	
	function getExpeditionPoints($expeditionId){
		$stmt = $this->db->prepare("SELECT latitude, longitude, altitude, expedition_id FROM gps_sample WHERE expedition_id= :expeditionId ");
		$stmt->bindValue(":expeditionId", $expeditionId);
		$stmt->execute();
		$temp = $stmt->fetchAll(PDO::FETCH_ASSOC);
		return $temp;
	}
	
	
	/**
	 * delete all finds
	 * @param unknown_type $projectId
	 */
	function deleteAllFinds($projectId) {
		Log::getInstance()->log("deleteAllFinds: $projectId");

		$stmt = $this->db->prepare("delete from find where project_id = :projectId");
		$stmt->bindValue(":projectId", $projectId);
		$stmt->execute();
	}
	/**
	 * delete the given project
	 * @param unknown_type $id
	 */
	function deleteProject($id) {
		Log::getInstance()->log("deleteProject: $id");

		$stmt = $this->db->prepare("delete from project where id = :id");
		$stmt->bindValue(":id", $id);
		$stmt->execute();
	}
	/**
	 * delete the image associated with the id
	 * @param unknown_type $findId
	 */
	function deleteImages($findId) {
		$stmt = $this->db->prepare("delete from photo where find_id = :findId");
		$stmt->bindValue(":findId", $findId);
		$stmt->execute();
		echo "Deletion of image with find_id = ".$findId." successful.";
	}
	/**
	 * delete all the videos associated with a find
	 * @param unknown_type $findId
	 */
	function deleteVideos($findId) {
		$stmt = $this->db->prepare("delete from video where find_id = :findId");
		$stmt->bindValue(":findId", $findId);
		$stmt->execute();
		echo "Deletion of video with find_id = ".$findId." successful.";
	}
	/**
	 * delete audio clips associated with a find
	 * @param unknown_type $findId
	 */
	function deleteAudioClips($findId) {
		$stmt = $this->db->prepare("delete from audio where find_id = :findId");
		$stmt->bindValue(":findId", $findId);
		$stmt->execute();
		echo "Deletion of audio clip with find_id = ".$findId." successful.";
	}
	/**
	 * Create  a new find
	 * @param unknown_type $guId
	 * @param unknown_type $projectId
	 * @param unknown_type $name
	 * @param unknown_type $description
	 * @param unknown_type $latitude
	 * @param unknown_type $longitude
	 * @param unknown_type $revision
	 */
	function createFind($imei, $guId, $projectId, $name, $description, $latitude, $longitude, $revision) {
		Log::getInstance()->log("createFind: $guId, $projectId, $name, $description, $latitude, $longitude, $revision");
		$stmt = $this->db->prepare(
			"insert into find (imei, barcode_id, project_id, name, description, 
			latitude, longitude, add_time, modify_time, revision) VALUES
			(:imei, :barcode_id, :projectId, :name, :description, :latitude, :longitude ,now(), now(), :revision)"
		);
		
		$stmt->bindValue(":imei", $imei);
		$stmt->bindValue(":barcode_id", $guId);
		$stmt->bindValue(":projectId", $projectId);
		$stmt->bindValue(":name", $name);
		$stmt->bindValue(":description", $description);
		$stmt->bindValue(":latitude", $latitude);
		$stmt->bindValue(":longitude", $longitude);
		$stmt->bindValue(":revision", $revision);
		$stmt->execute(); 
		
		$findid = $this->db->lastInsertId();
		Log::getInstance()->log("lastInsertId()=$findid");
		
		// Make an entry in find_history
		$stmt = $this->db->prepare(
			"insert into find_history (find_guid, action, imei) VALUES
			(:find_guid, :action, :imei)"
		);
		$stmt->bindValue(":find_guid", $guId);
		$stmt->bindValue(":action", "create");	
		$stmt->bindValue(":imei", $imei);	
		$stmt->execute();
		
		$lastid = $this->db->lastInsertId();
		Log::getInstance()->log("Updated find_history, created lastInsertId()=$lastid");

//		return $findid; //get the rowid where it's inserted so that the client can sync.. @todo update in API
		return "True Created $guId in row=$findid";  
	}
	/**
	 * Update information about a find
	 * @param unknown_type $guId -- globally unique ID
	 * @param unknown_type $name
	 * @param unknown_type $description
	 * @param unknown_type $revision
	 */
	function updateFind($imei, $guId, $projectId, $name, $description, $revision) {
		Log::getInstance()->log("updateFind: $imei, $guId, $projectId, $name, $description, $revision");
		$stmt = $this->db->prepare("update find set name = :name, description = :description, 
			revision = :revision, modify_time = NOW() where barcode_id = :guid AND project_id = :projectId");
		
		$stmt->bindValue(":name", $name);
		$stmt->bindValue(":description", $description);
		$stmt->bindValue(":revision", $revision);
		$stmt->bindValue(":guid", $guId);
		$stmt->bindValue(":projectId", $projectId);
		$stmt->execute();
		Log::getInstance()->log("Updated Find= $guId");

		// Make an entry in find_history
		$stmt = $this->db->prepare(
			"insert into find_history (find_guid, action, imei) VALUES (:find_guid, :action, :imei)"
		);
		$stmt->bindValue(":find_guid", $guId);
		$stmt->bindValue(":action", "update");	
		$stmt->bindValue(":imei", $imei);	
		$stmt->execute();
		Log::getInstance()->log("Updated find_history, updated Find $guId $imei");
		return "True Updated $guId on server";
	}
	
	/**
	 * Add a picture to the find
	 * @param unknown_type $id
	 * @param unknown_type $findId
	 * @param unknown_type $mimeType
	 * @param unknown_type $dataFull
	 * @param unknown_type $dataThumb
	 */
	 // $request["imei"], $request["guid"], $request["identifier"], $request["project_id"], 
	//		    $request["mime_type"], $request["timestamp"], $imagedata, $imagethumbdata);

	function addPictureToFind($imei, $guid, $identifier, $project_id,  $mime_type, $timestamp, $dataFull, $dataThumb) {
		Log::getInstance()->log("addPictureToFind: $imei, $guid, $identifier, $mimeType");

		$stmt = $this->db->prepare(
			"insert into photo (imei, guid, identifier, project_id,  mime_type, timestamp, data_full, data_thumb)
			           VALUES (:imei, :guid, :identifier, :project_id, :mime_type, :timestamp, :dataFull, :dataThumb)"
		);
		$stmt->bindValue(":imei",$imei);
		$stmt->bindValue(":guid",$guid);
		$stmt->bindValue(":identifier",$identifier);
		$stmt->bindValue(":project_id",$project_id);
		$stmt->bindValue(":mime_type",$mime_type);
		$stmt->bindValue(":timestamp",$timestamp);
		$stmt->bindValue(":dataFull",$dataFull);
		$stmt->bindValue(":dataThumb",$dataThumb);
		$stmt->execute();
		Log::getInstance()->log("addPictureToFind: $imei, $guid, $identifier, $mimeType");
		
		$lastid = $this->db->lastInsertId();
		Log::getInstance()->log("Updated photos for Find $guid, created record lastInsertId()=$lastid");
		return "True Created photo record $guId in row=$lastid";  
		//return $stmt->fetch(PDO::FETCH_ASSOC);
	}
	
	/**
	 * Add video to the find
	 * @param unknown_type $id
	 * @param unknown_type $findId
	 * @param unknown_type $mimeType
	 * @param unknown_type $dataPath
	 */
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
	/**
	 * Add audio clip to the find
	 * @param unknown_type $id
	 * @param unknown_type $findId
	 * @param unknown_type $mimeType
	 * @param unknown_type $dataPath
	 */
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
	/**
	 * deletes picture from the find
	 * @param unknown_type $id
	 */
	function deletePictureFromFind($id) {
		$stmt = $this->db->prepare(
			"delete from photo where id = :id"
		);
		$stmt->bindValue(":id", $id);
		$stmt.execute();
	}
	/**
	 * delete video from the find
	 * @param $id
	 */
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
	/**
	 * delete audio clip from a  find
	 * @param unknown_type $id
	 */
	function deleteAudioClipFromFind($id) {+
		$stmt = $this->db->prepare(
			"select data_path from audio where id = :id");
		$stmt->bindValue(":id", $id);
		$stmt.execute();
		$audio = $stmt->fetch(PDO::FETCH_ASSOC);
		$audio_path = $audio['data_path'];
		$fh = fopen("uploads/$audio_path", 'w') or die("[Error on server]Can't open file");
		fclose($fh);
		unlink($audio_path);
		$stmt = $this->db->prepare(
			"delete from audio where id = :id"
		);
		$stmt->bindValue(":id", $id);
		$stmt.execute();
	}
	/**
	 * register a new user
	 * @param $newUser
	 */
	function registerUser($newUser) {
		Log::getInstance()->log("registerUser: $newUser");
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
//		return true;
		return array($this->db->lastInsertId());
	}
	/**
	 * get all the devices the user has registered
	 * @param unknown_type $userId
	 */
	function getDevicesByUser($userId) {
		Log::getInstance()->log("getDevicesByUser: $userId");
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
	/**
	 * get devices by the key given
	 * @param unknown_type $authKey
	 */
	function getDeviceByAuthKey($authKey) {
//		Log::getInstance()->log("getDeviceByAuthKey: $authKey");
		$stmt = $this->db->prepare(
			"SELECT imei, name, user_id, add_time, status
			 FROM device
			 WHERE auth_key = :authKey
			 LIMIT 1"
		); $stmt->bindValue(":authKey", $authKey);
		$stmt->execute();
		return $stmt->fetch(PDO::FETCH_ASSOC);
	}
	/**
	 * registration pending verification
	 * @param unknown_type $userId
	 * @param unknown_type $authKey
	 */
	function registerDevicePending($userId, $authKey) {
//		print_r(array($userId, $authKey));
		Log::getInstance()->log("registerDevicePending: $userId, $authKey");
		if(!$userId || !$authKey) return false;
		$stmt = $this->db->prepare(
			"INSERT INTO device (user_id, auth_key, add_time)
			 VALUES (:userId, :authKey, now())"
		);
		print_r(mysql_error());

		$stmt->bindValue(":userId", $userId);
		$stmt->bindValue(":authKey", $authKey);
		$stmt->execute();
		
		return true;
	}
	/**
	 * confirm registration 
	 * @param unknown_type $authKey
	 * @param unknown_type $imei
	 * @param unknown_type $name
	 */
	function confirmDevice($authKey, $imei, $name) {
		Log::getInstance()->log("confirmDevice: $authKey, $imei, $name");

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
	/**
	 * add a device to the sandbox
	 * @param unknown_type $authKey
	 * @param unknown_type $imei
	 */
	function addSandboxDevice($authKey, $imei) {
		log("addSandboxDevice " .$authKey . " " . $imei);
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
	/**
	 * change/set the nickname of the device 
	 * @param unknown_type $imei
	 * @param unknown_type $name
	 */
	function changeDeviceNickname($imei, $name) {
		$stmt = $this->db->prepare(
			"UPDATE device SET name = :name WHERE imei = :imei"
		);
		$stmt->bindValue(":name", $name);
		$stmt->bindValue(":imei", $imei);
		return $stmt->execute();
	}
	/**
	 * remove a device from the database
	 * @param unknown_type $imei
	 */
	function removeDevice($imei) {
		$stmt = $this->db->prepare(
			"DELETE FROM device WHERE imei = :imei"
		);
		$stmt->bindValue(":imei", $imei);
		return $stmt->execute();
	}
	/**
	 * remove all the devices that didn't get verified
	 */
	function purgePendingDevices() {
		$stmt = $this->db->prepare("DELETE FROM device WHERE imei IS NULL");
		$stmt->execute();
	}
	/**
	 * search for finds
	 * @param $search_value
	 * @param $project_id
	 */
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
	/**
	 * generic execute command
	 * @param $command_value
	 */
	function execCommand($command_value){
		if ($command_value == "create_sample_text") {
			$file = "test/HelloWorld.txt";
			$handler = fopen($file, 'w') or die("can't open file");
			$data = "This is a sample text created in order to demonstrate the functionality of the command line";
			fwrite($handler, $data);
			fclose($handler);
		}
	}

	function addInstance($project_id, $name, $description, $sync_on, $auth_key){
		$stmt = $this->db->prepare("INSERT INTO instance (project_id, name, description, sync_on, auth_key) VALUES (:project_id, :name, :description, :sync_on, :auth_key)") or print_r($this->db->errorInfo()) && die();
		$stmt->bindValue(":project_id", $project_id);
		$stmt->bindValue(":name",$name);
		$stmt->bindValue(":description",$description);
                $stmt->bindValue(":sync_on", $sync_on);
		$stmt->bindValue(":auth_key", $auth_key);
		$stmt->execute();

		return true;	
	}
	

	function getInstancesForProject ($project_id){
		$stmt = $this->db->prepare(
			"SELECT id, name, description, project_id, sync_on, auth_key
			FROM instance
			WHERE project_id = :project_id"
			) or print_r($this->db->errorInfo()) && die();
		$stmt->bindValue(":project_id", $project_id);
		$stmt->execute();
		$available_values = array();
		$temp = $stmt->fetchAll(PDO::FETCH_ASSOC);
		
		foreach($temp as $value){
			$available_values[] = $value;
		}
		return $available_values;

	}
}

?>
