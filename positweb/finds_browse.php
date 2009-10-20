<?php

/*
	
	POSIT Web
	@author: Jwalanta Shrestha
	
	Finds Browser
	
*/

require "includes/bootstrap.php";

if (!isset($_GET['id'])) pageRedirect('index.php');

if (!$isLogged) die("You need to be logged on.");
if ($currentUserGroup['name']!='Admin') die("You need to be admin.");

// ajax call from grid
if (isset($_GET['grid_data'])){
	
	$id = cleanup($_GET['id']);
	$s = new FindsBrowse($id);
	$s->set_grid_options();
	echo $s->grid_json();
	
	die();
}

$pageTitle .= " - Finds Browser";
$extracss = array("css/flexigrid.css");
$extrajs = array($jqueryPath,"js/flexigrid.pack.js");
include "includes/pageheader.php";

?>
<div id='scaffoldbody'>
<div class='container'>
<?php 

$id = cleanup($_GET['id']);
$s = new FindsBrowse($id); 

echo "<h2>Find data for ".$s->project_row['name']."</h2>";
	
?>

<table id='findtable'></table>
</div>
</div>
<script>
$(document).ready(function(){
	$('#findtable').flexigrid(<?php echo $s->get_grid_options_json(); ?>);
});

</script>
<?php
include "includes/pagefooter.php";
?>

<?php

Class FindsBrowse{
	
	var $finds_table = '';
	var $id='';
	var $project_row = array();
	
	var $row_odd = '#fff';						// odd number row color
	var $row_even = '#eee';						// even number row color	
	
	var $exclude_cols = array('id', 'project_id', 'device_id','latitude','longitude','updated_at', 'created_at', 'post_data','revision');
	
	
	
	function FindsBrowse($project_id){
		
		$this->id = $project_id;
		
		$result = mysql_query("SELECT * FROM projects WHERE id='$project_id'");
		if (mysql_num_rows($result)>0){
			$row = mysql_fetch_assoc($result);
			$this->project_row = $row;
			$this->finds_table = 'finds_'.$row['id'].'_'.preg_replace('/[^a-z0-9_]/','',str_replace(" ","_",strtolower($row['name'])));
		}	
		else die('Invalid ID');
		
		
		
	}
	
	function list_table($msg = NULL, $where = null){
		
		$query = "SELECT * FROM $this->finds_table";

		$select = mysql_query($query) or die(mysql_error());
		
		$page .= '<table id="findtable">';
		$page .= '<tr>';
		while($i < mysql_num_fields($select)){
			$column = mysql_fetch_field($select, $i);
			if(array_search($column->name, $this->exclude_cols)===FALSE){
				$page .= '<th nowrap>'.$this->build_friendly_names($column->name).'</th>';
			}
			$i++;
		}
		$page .= '</tr>';

		$count = 0;
		while($array = mysql_fetch_array($select)){
			$page .= (!($count % 2) == 0) ? '<tr style="background:'.$this->row_even.';">' : '<tr style="background:'.$this->row_odd.';">';
			foreach($array as $column => $value){
				if(!is_int($column) && array_search($column, $this->exclude_cols)===FALSE){
					$page .= '<td>';
					if($this->htmlsafe) {
						$page .= htmlentities($value);
					}else{
						$page .= $value;
					}
					$page .= '</td>';
				}
			}
			$count ++;
			$page .= '</tr>';
		}

		$page .= "</table>";
		
		echo "<h2>{$this->project_row['name']}</h2>";
		echo $page;
	}
	
	function set_grid_options(){
		
		global $_POST;

		$sortname = $_POST['sortname'];
		$sortorder = $_POST['sortorder'];
		
		if (!$sortname) $sortname = 'find_time';
		if (!$sortorder) $sortorder = 'desc';
		if($_POST['query']!=''){
			$this->where = "WHERE `".$_POST['qtype']."` LIKE '%".$_POST['query']."%' ";
		} else {
			$this->where ='';
		}

		$this->sort = "ORDER BY $sortname $sortorder";
		
		$this->page = $_POST['page'];
		if (!$this->page) $this->page = 1;

		$this->rp = $_POST['rp'];
		if (!$this->rp) $this->rp = 10;
		
		$start = (($this->page-1) * $this->rp);
		
		$this->limit = "LIMIT $start, $this->rp";
		
	}
	
	function get_grid_options_json(){
		$o = "{\n";
		$o .= "url: 'finds_browse.php?id={$this->id}&grid_data',\n";
		$o .= "dataType: 'json',\n";
		$o .= "colModel : [\n";
		
		$query = "SELECT * FROM $this->finds_table";
		$select = mysql_query($query) or die(mysql_error());
		
		$colarr = array();
		$searcharr = array();
		
		$colarr[] = "{display: 'ID', name : 'id', sortable : true, align: 'left', width: 20}";
		$colarr[] = "{display: 'Device', name : 'device', sortable : true, width: 50, align: 'left'}";
		$colarr[] = "{display: 'Coordinate', name : 'coordinate', sortable : true, width: 70, align: 'left'}";
		while($i < mysql_num_fields($select)){
			$column = mysql_fetch_field($select, $i);
			if(array_search($column->name, $this->exclude_cols)===FALSE){
				$colarr[] = "{display: '".$this->build_friendly_names($column->name)."', name : '$column->name', width: 80, sortable : true, align: 'left'}";
				if (strtolower($column->type)=='blob'){
					$searcharr[]="{display: '".$this->build_friendly_names($column->name)."', name : '$column->name'}";
					
				}
			}
			$i++;
		}
		$o .= implode(",\n",$colarr);

		$o .= "],\n"; // end colModel
		
		$o .= "searchitems : [\n";
		$o .= implode(",\n",$searcharr);
		$o .= "],\n"; // end searchitems
		
		$o .= "sortname: 'id',\n";
		$o .= "sortorder: 'asc',\n";
		$o .= "usepager: true,\n";
		$o .= "title: '{$this->project_row['name']}',\n";
		$o .= "useRp: true,\n";
		$o .= "rp: 15,\n";
		$o .= "width: 800,\n";
		$o .= "height: 360\n";
		$o .= "}";
		
		return $o;
	}
	
	function grid_json(){
		$sql = "SELECT * FROM $this->finds_table $this->where";
		$result = mysql_query($sql);		
		$total = mysql_num_rows($result);
		
		$sql = "SELECT * FROM $this->finds_table $this->where $this->sort $this->limit";
		$result = mysql_query($sql);
				
		$json = "";
		$json .= "{\n";
		$json .= "page: $this->page,\n";
		$json .= "total: $total,\n";
		$json .= "rows: [";
		
		$rc = false;
		while($row = mysql_fetch_array($result)){
			if ($rc) $json .= ",";
			$json .= "\n{";
			$json .= "id:'".$row['id']."',";
			$json .= ("cell:['{$row['id']}','".$this->device_id_to_name($row['device_id'])."','({$row['latitude']},{$row['longitude']})'");
			
			foreach($row as $column => $value){

				if(!is_int($column) && array_search($column, $this->exclude_cols)===FALSE){
					$json .= ",'".htmlentities($value)."'";
				}
				
			}
			$json .= "]}";
			$rc = true;

		}
		$json .= "]}";
		
		return $json;		
		
	}

	
	function build_friendly_names($field) {
		return ucwords(str_replace('_', ' ', $field));
	}
	
	function device_id_to_name($id){
		$sql = "SELECT * FROM devices WHERE id='$id'";
		$result = mysql_query($sql);
		$row = mysql_fetch_assoc($result);
		
		return $row['name'];
	}
}



?>
