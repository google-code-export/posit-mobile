<?php

/*
	
	POSIT Web
	@author: Jwalanta Shrestha
	
	Finds in Google Maps
	
*/

require "includes/bootstrap.php";

if (!isset($_GET['id'])) pageRedirect('index.php');

if (!$isLogged) die("You need to be logged on.");
if ($currentUserGroup['name']!='Admin') die("You need to be admin.");

$pageTitle .= " - Finds Map";
// localhost
// $googleMapKey = 'ABQIAAAA2ney3--W-AMvOp1awafflxT2yXp_ZAY8_ufC3CFXhHIE1NvwkxTeK13n71pbiwmYqeOCGW2MdcLXgg';

// dev.posit-project.org
$googleMapKey = 'ABQIAAAA2ney3--W-AMvOp1awafflxSw2KTuq9orgXWjklIZzm1TKFLhTBTpOrpxuzXcctpgda4PKXbA3YuNPQ';

$extrajs = array("http://maps.google.com/maps?file=api&amp;v=2&amp;sensor=false&amp;key=$googleMapKey");
include "includes/pageheader.php";

?>
<div id='findmaps'>
<div class='container'>
<?php 

$id = cleanup($_GET['id']);
$m = new FindsMap($id); 

echo "<h2>Find Map for ".$m->project_row['name']."</h2>";
	
?>

<div id="findsmap" style="width: 800px; height: 450px"></div>

<noscript>
<div class='notify-error'>
  <b>JavaScript must be enabled in order for you to use Google Maps.</b> 
  However, it seems JavaScript is either disabled or not supported by your browser. 
  To view Google Maps, enable JavaScript by changing your browser options, and then 
  try again.
  </div>
</noscript>

<?php

?>

</div>
</div>
<script>

if (GBrowserIsCompatible()) { 
	
	
	
	function createMarker(lat, lng, html){
		var point = new GLatLng(lat, lng);
		var marker = new GMarker(point);
		bounds.extend(point);
		
		GEvent.addListener(marker,"click",function(){ 
			marker.openInfoWindowHtml(html);
		});
		return marker;
	}
	
	var map = new GMap2(document.getElementById("findsmap"));
	var bounds = new GLatLngBounds();
	
	map.addControl(new GLargeMapControl());
	map.addControl(new GMapTypeControl());
	
	map.setCenter(new GLatLng(0,0),0);
	
	/*
	
	map.addOverlay(createMarker(43.65654,-79.90138,"hello world1"));
	map.addOverlay(createMarker(43.91892,-78.89231,"hello world2"));
	map.addOverlay(createMarker(43.82589,-79.10040,"hello world3"));
	
	*/
	
	<?php 
		echo $m->addMarkersCode();
	?>
	
	map.setZoom(map.getBoundsZoomLevel(bounds));
	map.setCenter(bounds.getCenter());
	
}

// display a warning if the browser was not compatible
else {
  alert("Sorry, the Google Maps API is not compatible with this browser");
}

</script>

<?php
include "includes/pagefooter.php";
?>

<?php

Class FindsMap{
	
	var $finds_table = '';
	var $id='';
	var $project_row = array();
	
	var $exclude_cols = array('id', 'project_id', 'device_id','latitude','longitude','updated_at', 'created_at', 'post_data','revision');

	function FindsMap($project_id){
		
		$this->id = $project_id;
		
		$result = mysql_query("SELECT * FROM projects WHERE id='$project_id'");
		if (mysql_num_rows($result)>0){
			$row = mysql_fetch_assoc($result);
			$this->project_row = $row;
			$this->finds_table = 'finds_'.$row['id'].'_'.preg_replace('/[^a-z0-9_]/','',str_replace(" ","_",strtolower($row['name'])));
		}	
		else die('Invalid ID');
	}
	
	function addMarkersCode(){
		$query = "SELECT * FROM $this->finds_table";
		$result = mysql_query($query) or die(mysql_error());
		
		$code = '';
		while ($row = mysql_fetch_assoc($result)){
			$lat = $row['latitude'];
			$lng = $row['longitude'];
			$text = "<div style=\"font: 12px Verdana,sans-serif;\">".$row['description']."<br /><br /></div><div style=\"font: 9px Verdana, sans-serif; \"><em>".$row['find_time']."</em></div>";
			
			$code .= "map.addOverlay(createMarker($lat,$lng,'$text'));\n";

		}
		
		echo $code;
	}
	
	
	
	
	
}
