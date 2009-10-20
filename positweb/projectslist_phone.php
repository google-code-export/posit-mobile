<?php
  /*
   * A quickish code to get all the projects in the server 
   * Supposed to be changed later, once these things get finalized.
   */
  
  require_once("includes/bootstrap.php");
  $result = mysql_query("SELECT * FROM projects");
  $projects = array();
  while ($row = mysql_fetch_assoc($result)){
	array_push($projects,$row);
  }
  echo json_encode($projects);
?>