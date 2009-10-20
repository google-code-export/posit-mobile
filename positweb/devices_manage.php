<?php
$title = "Registered Devices";
$table = "devices";

require "includes/bootstrap.php";

if (!$isLogged) die("You need to be logged on.");
if ($currentUserGroup['name']!='Admin') die("You need to be admin.");

require "includes/scaffold_class.php";

$pageTitle .= " - Manage $title";

include "includes/pageheader.php";

?>
<div id='scaffoldbody'>
<div class='container'>
<h2><?php echo "Manage $title"; ?></h2>
<?php $s = new Scaffold($table,100,array('name','imei'),true, 300); ?>
</div>
</div>
<?php
include "includes/pagefooter.php";
?>
