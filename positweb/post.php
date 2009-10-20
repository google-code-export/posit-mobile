<?
/* Note: only for test, remember to delete */
/*$dbserver = "localhost"
$dbuser = "testuser";
$dbpassword = "testpasswordx";
$dbname = "testuser_post";
$con = mysql_connect($dbserver, $dbuser,$dbpassword) or die("cannot connect to database");
if (in_array("saving", $_POST){
	$sql = "insert into finds (".implode(",",$_POST["find"]).")";
	mysql_query($sql, $connection);
}
*/


foreach ($_POST as $k=>$v){
	if ($k== "saving"){
		echo "got it";
		print_r( $_POST);
		exit;
	}
	if ($k == "extract"){
		$arr = array();
		for ($i = 1; $i<=5; $i++){
			$arr[$i] = array("id"=>$i,"name"=>"test","type"=>"x");	
		}
		echo json_encode($arr);
		exit;
	}
}
echo "didn't get it";


?>

