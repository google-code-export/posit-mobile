<?php
/**
 * ver 0.9.4
 * Basic CRUD system
 * PHP 4.3 and up
 *
 * @author Nick Morlan - nick@buildmyblog.com 2007.10.04
 *
 
 *IMPORTANT*
	 Some basic database structure is assumed for the scaffold to run smoothly..
	 Table names are all plural and lowercase.
	 Primary keys are all named 'id' and AUTO_INCREMENT.
	 Field names are lowercase and can be underscored .. ie-> 'first_name'.
	 Timestamps named 'created_at' and 'updated_at' cannot be edited, they will be
	 updated automatically.
	 Foreign keys are named as {table_name_singluar_id}
				'author_id' references table 'authors'
				'book_id' references table 'books'
				'category_id' references table 'categories'
				and so on.
	 Foreign key tables also ARE ASSUMED TO have a field 'name'.
	 The databases don't have a field named 'xxbmnnzhfwggsf'
				
	 Special case for plurals can be put in the configuration so 'person_id' can reference
	 table 'people'. See setup below.
 
 This scaffold will generate basic CRUD (Create, Read, Update, Delete) functionality
 for administering databases. Include this file in your page and create the scaffold by
 calling <?php new Scaffold("TABLE_NAME"); ?> You can pass an optional second interger 
 parameter to limit how many records show up per page, the default is 100.
 
 You can customize the display to show only selected fields by passing an optional third 
 array parameter containing the field names you want to show.
 
 The listings will show up html safe, you can change by passing an optional fourth
 boolean parameter, default is TRUE.
 
 To fit into a page you can pass an optional fif integer parameter specifying your
 preferred table width.
 
 An example with all parameters passed..
 <?php new Scaffold("TABLE_NAME", 25, array('name', 'score'), FALSE, 400); ?>

 The foriegn keys will show up as dropdown menus on the edit screen and display the 
 'name' field if present, otherwise it will show the 'id'.

 Change the configuration section to match your database setup to get started.
 
 *USE AT OWN RISK*
 This code is offered AS IS and there is no garuantee to the usability or functionality
 of it and using it may cause your website to catch on fire.
 
 Released under Creative Commons Liscense
 http://creativecommons.org/licenses/by/3.0/legalcode
 	Use, hack apart and redistribute (even commercially) but keep original credit in tact.
 
  
  CHANGELOG
  0.9.4 Added search functionality for listings.
  		Made listing page show instead of intermediary page after updating records.
  		Scrub the rest of the post data going into SQL calls including ID's just in case.. 
  		  maybe somebody was to use CURL and fuss with things
  		Clean up mixed use of single and double quotes in display
  
  */

class Scaffold {

	/* begin configuration */
	// change the values in the configuration section to match your setup

	/**
	* Connection variables
	* Replace these with your own info
	*/
	
	var $db_host = 'localhost';
	var $db_user = 'root';
	var $db_password = '';
	var $db_name = 'posit';
	
	/**
	* Special Case Tables
	* Replace or add to these arrays with your own info if you are using a table with 
	* an odd way of expressing plurals. These are used in building the 
	* foreign key relationships.
	*
	* ie-> your table is named people and your foreign key 
	* would be person_id
	* This will explicitly set your foreign key table realtionship(s)
	* Put all special case plurals in the arrays below or leave arrays blank
	*/
	var $singular = array ('person');			// for foreign key(s)
	var $plural = array ('people');			// for foreign key(s)
	
	/**
	* alternating row colors
	*/
	var $row_odd = '#fff';						// odd number row color
	var $row_even = '#eee';						// even number row color
	
	/* end configuration */
	
	
	var $table = '';								// internal var for table
	
	function Scaffold($table, $max_records = 100, $fields = array(),  $htmlsafe = true, $width = NULL){
		global $dbserver, $dbusername, $dbpassword, $dbname;
		
		$this->db_host = $dbserver;
		$this->db_user = $dbusername;
		$this->db_password = $dbpassword;
		$this->db_name = $dbname;
		
		$this->table = $table;						// sets the database table
		$this->max_records = intval($max_records);	// sets the limit on how many records are displayed per page
		$this->fields = $fields;					// sets the fields display
		$this->htmlsafe = $htmlsafe;				// make display html safe
		$this->width = intval($width);				// width of listing table
		// the following sets the page variable
		(!empty($_GET['page']) && is_numeric($_GET['page'])) ? $this->page = intval($_GET['page']) : $this->page = 1;
		
		
		
		
		$connection = mysql_connect($this->db_host,
							$this->db_user,
							$this->db_password);
		mysql_select_db($this->db_name, $connection) or die('There was an error connecting to the databse.. check your connection settings.');

		$action = (!empty($_POST['xxbmnnzhfwggsf'])) ? $_POST['xxbmnnzhfwggsf'] : 'list' ;
		switch($action){
			default:
				$this->list_table();
			break;

			case 'list':
				$this->list_table();
			break;

			case 'new':
				$this->new_row();
			break;

			case 'create':
				$this->create();
			break;

			case 'edit':
				$this->edit_row();
			break;

			case 'update':
				$this->update();
			break;

			case 'delete':
				$this->delete_row();
			break;
			
			case 'search':
				$this->search();
			break;
		}
	}

	/**
	* This method builds the record listing
	*
	* string $msg 		// pass an optional message to be displayed
	* strgin $where		// pass an optional WHERE parameter SQL call 
	*
	*/
	function list_table($msg = NULL, $where = null){
		$start = (($this->page-1)*$this->max_records);				// start parameter for pages
		$end = $this->max_records;									// end parameter for pages
		$page = '';													// var to buiild display
		$totalQuery = mysql_query ('SELECT COUNT(*) FROM '.$this->table.$where) or die(mysql_error());
		$totalA = mysql_fetch_array($totalQuery);
		$total = $totalA[0];
		
		if (!empty($this->fields)) {
		// just display the selected fields
			$query = 'SELECT id';
			foreach($this->fields as $val){
				$query .= ', '.$val;
			}
			$query .= ' FROM '.$this->table;
		}else{
			$query = 'SELECT * FROM '.$this->table;
		}
		if(!empty($where)){ $query .= $where; }
		$query = $query.' LIMIT '.$start.', '.$end;
		$select = mysql_query($query) or die(mysql_error());
		$i = 0;
		
		(!empty($this->width)) ? $width = ' width="'.$this->width.'"' : $width = NULL;
		
		$this->build_search_bar();
		echo $total.' Total Records. | '
			. '<form name="newrecord" id="newrecord" action="'.$_SERVER['PHP_SELF'].'" method="post" style="display:inline"><input type="hidden" name="xxbmnnzhfwggsf" value="new"><a href="javascript:document.newrecord.submit()">New Record</a></form><br /><br />';
		if(!empty($msg)) { echo $msg; }
		$this->paginate($total, $this->page);
		$page .= '<table cellpadding="2" cellspacing="0" border="0"'.$width.'">';
		$page .= '<tr>';
		while($i < mysql_num_fields($select)){
			$column = mysql_fetch_field($select, $i);
			if($column->name != 'id' && $column->name != 'updated_at' && $column->name != 'created_at'){
				$page .= '<th nowrap>'.$this->build_friendly_names($column->name).'</th>';
			}
			$i++;
		}
		$page .= '</tr>';

		$count = 0;
		while($array = mysql_fetch_array($select)){
			$page .= (!($count % 2) == 0) ? '<tr style="background:'.$this->row_even.';">' : '<tr style="background:'.$this->row_odd.';">';
			foreach($array as $column => $value){
				if(!is_int($column) && $column != 'id' && $column != 'updated_at' && $column != 'created_at'){
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
			$page .= '<td><form name="edit_'.$array[0].'" id="edit_'.$array[0].'" action="'.$_SERVER['PHP_SELF'].'" method="post" style="display:inline"><input type="hidden" name="xxbmnnzhfwggsf" value="edit"><input type="hidden" name="id" value="'.$array[0].'"><a href="javascript:document.edit_'.$array[0].'.submit()">Edit</a></form></td>
				<td>
				<form name="delete_'.$array[0].'" id="delete_'.$array[0].'" action="'.$_SERVER['PHP_SELF'].'" method="post" style="display:inline"><input type="hidden" name="xxbmnnzhfwggsf" value="delete"><input type="hidden" name="id" value="'.$array[0].'"><a href="javascript:" onClick="if (confirm(\'Are you sure?\')){document.delete_'.$array[0].'.submit();}else{return false;}">Delete</a></form></td>';
			$page .= '</tr>';
		}

		$page .= "</table>";
		echo $page;
		$this->paginate($total);      
		
		
		}

	/**
	* This method builds a new record form page
	*
	*/
	function new_row(){
		$page = '';
		$selectFields = mysql_query('SELECT * FROM '.$this->table);
		$i = 0;
		$page .= '<form action="'.$_SERVER['PHP_SELF'].'" method="POST">'
				. '<input type="hidden" name="xxbmnnzhfwggsf" value="create">'
				. '<table cellpadding="2" cellspacing="0" border="0">';
		while($i < mysql_num_fields($selectFields)){
			$column = mysql_fetch_field($selectFields);
			if($column->name != 'id'){
				$page .= '<tr>';
				// check for foreign keys..
				if(substr($column->name, -3) == '_id'){
					$page .= $this->build_foreign_key_dropdowns($column->name);
				}elseif($column->blob == 1){
					$page .= '<td valign="top"><strong>'.$this->build_friendly_names($column->name).':</strong></td><td> <textarea name="'.$column->name.'" rows="10" cols="45"></textarea></td>';
				}elseif($column->type == 'timestamp'){
					$page .= '<input type="hidden" name="'.$column->name.'" />';
				}else{
					$page .= '<td><strong>'.$this->build_friendly_names($column->name).':</strong></td><td><input type="text" name="'.$column->name.'" value="" size="35" /></td>';
				}
			}
			$i++;
		}
		$page .= '<tr><td>&nbsp;</td><td><input type="submit" value="Add New Record" /></td></tr>'
		 		. '</table>'
		 		. '</form>'
		 		. '<a href="'.$_SERVER['PHP_SELF'].'">Back To Listings</a>';
		echo $page;
	}
	
	

	/**
	* This method inserts a new record
	* It is assumed that there is not a databse field named 'xxbmnnzhfwggsf'
	* That is my control variable for post navigation
	*
	*/
	function create(){
		$select = mysql_query('SELECT * FROM '.$this->table);
		$insert = 'INSERT INTO '.$this->table.' VALUES(\'\', ';
		$i = mysql_num_fields($select);
		$i--;
		foreach($_POST as $key => $value){
			if($key != 'xxbmnnzhfwggsf'){
				($key == 'updated_at' || $key == 'created_at')? $value = 'NOW()' : (get_magic_quotes_gpc) ? $value =  "'".mysql_real_escape_string(stripslashes($value))."'" : $value = "'".mysql_real_escape_string($value)."'"; 
				$i--;
				if($i > 0){
					$insert .= $value.", ";
				}
			}
		}
		$insert .= $value.')';
		mysql_query($insert) or die(mysql_error());
		$last_idq = mysql_query('SELECT LAST_INSERT_ID()')or die(mysql_error());
		$last_id = mysql_fetch_array($last_idq);
		$this->list_table('<div style="color:#090;"><p>Record Created..</p></div>');
	}

	/**
	* This method builds the edit record form page
	*
	*/
	function edit_row(){
		$page = '';
		$fields = mysql_query('SELECT * FROM '.$this->table) or die(mysql_error());
		$select = mysql_query('SELECT * FROM '.$this->table.' WHERE id = '.intval($_POST['id']));
		$row = mysql_fetch_row($select);
		$i = 0;
		
		$page .= '<form action="'.$_SERVER['PHP_SELF'].'" method="POST">'
				. '<input type="hidden" name="xxbmnnzhfwggsf" value="update">'
		 		. '<table cellpadding="2" cellspacing="0" border="0">';
		while($i < mysql_num_fields($fields)){
			$field = mysql_fetch_field($fields);
			if($field->name != 'id'){
				$page .= '<tr>';
				// check for foreign keys..
				if(substr($field->name, -3) == '_id'){
					$page .= $this->build_foreign_key_dropdowns($field->name, $row[$i]);
				}elseif($field->blob == 1){
					$page .= '<td valign="top"><strong>'.$this->build_friendly_names($field->name).':</strong></td><td> <textarea name="'.$field->name.'" rows="10" cols="45">'.$row[$i].'</textarea></td>';
				}elseif($field->type == 'timestamp'){
					$page .= '<td><strong>'.$this->build_friendly_names($field->name).':</strong></td><td>'.$row[$i].'</td>';
				}else{
					$page .= '<td><strong>'.$this->build_friendly_names($field->name).':</strong></td><td> <input type="text" name="'.$field->name.'" value="'.$row[$i].'" size="35" /></td>';
				}

			}else{
				$page .= '<td><strong>'.$this->build_friendly_names($field->name).':</strong></td><td>'.$row[$i].'<input type="hidden" name="id" value="'.$row[$i].'"></td>';
			}
			$i++;
			$page .= '</tr>';
		}
		$page .= '<tr><td>&nbsp;</td><td><input type="submit" value="Edit Record" /></td></tr>'
				 . '</table>' 
				 . '</form>'
				 . '<a href="'.$_SERVER['PHP_SELF'].'">Back To Listings</a>';
				 
		echo $page;
	}

	/**
	* This method updates the record
	*
	*/
	function update(){
		$select = mysql_query('SELECT * FROM '.$this->table.' WHERE id = '.intval($_POST['id']));
		$num = mysql_num_fields($select);
		$update = 'UPDATE '.$this->table.' SET ';
		$i = 1;
		$comma = '';
		while($i <= $num){
			$column = mysql_fetch_field($select);
			if($column->name != 'id' && $column->name != 'created_at' && $column->name != 'updated_at'){
					$update .= $comma.$column->name.' = ';
					$update .= (get_magic_quotes_gpc) ? '\''.mysql_real_escape_string(stripslashes($_POST["$column->name"])).'\'' : '\''.mysql_real_escape_string($_POST["$column->name"]).'\'';
					$comma =', ';
			}
			$i++;
		}
		$update .= '  WHERE id = '.intval($_POST['id']);
		mysql_query($update) or die(mysql_error());
		$this->list_table('<div style="color:#090;"><p>Record Modified..</p></div>');
	}

	/**
	* This method deletes a record
	*
	*/
	function delete_row(){
		mysql_query('DELETE FROM '.$this->table.' WHERE id = '.$_POST['id']) or die(mysql_error());
		$this->list_table('<div style="color:#900;"><p>Record Deleted..</p></div>');
	}

	/**
	* This method builds the search to be passed to list_table
	*
	*/
	function search(){
		if(!empty($_POST['searchterm'])){
			// safety first... 
			$searchterm = (get_magic_quotes_gpc) ? '\''.mysql_real_escape_string(stripslashes($_POST['searchterm'])).'\'' : '\''.mysql_real_escape_string($_POST['searchterm']).'\'';
			// just in case.. who knows with curl utils
			$field = (get_magic_quotes_gpc) ? '\''.mysql_real_escape_string(stripslashes($_POST['field'])).'\'' : '\''.mysql_real_escape_string($_POST['field']).'\'';
			switch ($_POST['compare']){
				default:
					$compare = '1';
					$compare = NULL;
					$searchterm = NULL;
					break;
				case '=':
					$compare = ' = ';
					break;
				case '>':
					$compare = ' > ';
					break;
				case '<':
					$compare = ' < ';
					break;
				case 'LIKE':
					$compare = ' LIKE ';
					$searchterm = (get_magic_quotes_gpc) ? "'%".mysql_real_escape_string(stripslashes($_POST["searchterm"]))."%'" : "'%".mysql_real_escape_string($_POST["searchterm"])."%'";
					break;
			
			}
			$where = ' WHERE '.$_POST['field'].$compare.$searchterm;
		}else{
			$where = NULL;
		}
		$this->list_table('<div style="color:#090">Search Results..</div>',$where);
		
	}

	
	/**
	* **IMPORTANT:**
	* table names must be plural, foriegn keys must be singular_id
	* and foreign table must have a field 'name'
	* as standard convention for following to work properly
	* Converts y to ies's ie- catagory to categories
	* CONSIDER: adding more to hash table for odd plurals.. ie -> people & person_id
	*
	* This method builds table rows with a name and dropdown menu
	* for the foreign key
	*
	* string $field				// foreign key field
	* string $value				// value to have initially selected in the dropdown
	*/
	function build_foreign_key_dropdowns($field, $value = null) {	
		// check for user defined foreign key relationships
		$match = FALSE;
		$dd = '';
		for($i=0; $i<count($this->singular); $i++){
			$match = preg_match('/^'.$this->singular[$i].'$/', substr($field, 0, -3));
			if($match){break;}
		}
		if($match){			
			$foreignTable = str_replace($this->singular, $this->plural, substr($field, 0, -3));
		}else{
			// break off trailing '_id' and pluralize name
			$foreignTable = substr($field, 0, -3);
			(substr($foreignTable, -1) != 'y') ? $foreignTable .= 's' : $foreignTable = substr($foreignTable, 0, -1).'ies';
		}
		$select = mysql_query('SELECT id, name FROM '.$foreignTable) or die(mysql_error().'SELECT id, name FROM '.$foreignTable);
		$foreign = mysql_fetch_assoc($select);
		$dd .= '<td><strong>'.$this->build_friendly_names(substr($field, 0, -3)).'</strong></td><td>'
		. '<select name="'.$field.'"';
		do{
			$dd .= "<option value='".$foreign['id']."'";
			if ($foreign['id'] == $value){ $dd .= ' selected';}
			if (!empty($foreign['name'])){
				$dd .= '>'.$foreign['name'].'</option>';
			}else{
				$dd .= '>'.$foreign['id'].'</option>';
			}
		}while($foreign = mysql_fetch_assoc($select));
		$dd .= '</td>';
		return $dd;
	}
	
	/**
	* This method builds the pagination
	*
	* int $total 	// pass the total number of rows in the table
	*
	*/
	function paginate($total = 1) {		
		// pagination
		if($total>$this->max_records){
		// Build the recordset paging links
		$num_pages = ceil($total / $this->max_records);
		$nav = '';
		
		// Can we have a link to the previous page?
		if($this->page > 1)
		$nav .= '<a href="'.$_SERVER['PHP_SELF'].'?page=' . ($this->page-1) . '">&lt;&lt; Prev</a> |';
		
		for($i = 1; $i < $num_pages+1; $i++)
		{
		if($this->page == $i)
		{
		  // Bold the page and dont make it a link
		  $nav .= ' <strong>'.$i.'</strong> |';
		}
		else
		{
		  // Link the page
		  $nav .= ' <a href="'.$_SERVER['PHP_SELF'].'?page='.$i.'">'.$i.'</a> |';
		}
		}
		
		// Can we have a link to the next page?
		if($this->page < $num_pages)
		$nav .= ' <a href="'.$_SERVER['PHP_SELF'].'?page=' . ($this->page+1) . '">Next &gt;&gt;</a>';
		
		// Strip the trailing pipe if there is one
		$nav = ereg_replace('@|$@', "", $nav);
		echo $nav;
		}
	}
	
	/**
	* This method builds the search bar display
	*
	*/
	function build_search_bar() {
		// build the fields menu
		$fielddropdown = '<select name="field">';
		$fieldselect = mysql_query('SHOW FIELDS FROM '.$this->table);
		while($fields = mysql_fetch_assoc($fieldselect)){
			$fielddropdown .= '<option value="'.$fields['Field'].'">'.$this->build_friendly_names($fields['Field']).'</option>';
		}
		$fielddropdown .= '</select>';
		$searchterm = (!empty($_POST['searchterm'])) ? $_POST['searchterm'] : '' ;
		$search = '';
		$search .=  '<form name="searchbar" id="searchbar" action="'.$_SERVER['PHP_SELF'].'" method="post" style="display:inline;"><input type="hidden" name="xxbmnnzhfwggsf" value="search">'
				. $fielddropdown
				. '<select name="compare">'
				. '<option value="=">Is Equal To</option>'
				. '<option value="LIKE">Contains</option>'
				. '<option value="<">Is Less Than</option>'
				. '<option value=">">Is Greater Than</option>'
				. '</select>'
				. '<input type="text" name="searchterm" value ="'.$searchterm.'">'
				. '<input type="submit" name="Search" value="Search" />'		
				. '</form><br /><br />';
				
		echo $search;
		
	}
	
	
	/**
	* This method returns reader friendly names
	* It will swap underscores with spaces and capitalize words for display
	*
	* string $field 	// pass the field name
	*
	*/
	function build_friendly_names($field) {
		return ucwords(str_replace('_', ' ', $field));
	}

}
