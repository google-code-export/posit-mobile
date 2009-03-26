<?php

require_once("includes/bootstrap.php");
if ($isLogged) {
	// call for logout
	
	setcookie('posituser','', time()- 3600);
	setcookie('positpass','', time()- 3600);
	
	pageRedirect("index.php");
}

if ($_POST['username']){
	
	// Setup variables
	$uname = cleanup($_POST["username"]);
	$upass = md5(cleanup($_POST["password"]));
	$remember = cleanup($_POST["rememberme"]);
	
	// check for validity
	$result = mysql_query(sprintf("SELECT * FROM users WHERE username='$uname' AND password='$upass'"));

    if (mysql_num_rows($result)>0){

    	// write cookies
        if ($remember=='1'){
	        setcookie('posituser',$uname, time()+ 24*3600);
    	    setcookie('positpass',$upass, time()+ 24*3600);
        }
        else{
        	setcookie('posituser',$uname);
    	    setcookie('positpass',$upass);
        }

        pageRedirect("index.php");;
        exit();
    }
    else $loginerror = "Incorrect username or password";
}

include "includes/pageheader.php";
?>

<div id='mainbody'>
	<div class='container'>
		<div id='loginbox'>
			<h2>Login</h2>
			<?php if ($loginerror!='') echo "<div class='errormsg'>$loginerror</div>\n"; ?>
			<form method="POST" action="<?php echo $_SERVER['PHP_SELF']; ?>">
				<?php 
				insertTextBox('username','Username',20,50); 
				insertPasswordBox('password','Password',20,50); 
				?>
				<div class='inputbox'><label><input type='checkbox' value="1" title="Remember Me"> Remember me</label></div>
				<div class='inputbox'><input type='submit' value="Login"></div>
			</form>
		</div>
	</div>
</div>

<?php include "includes/pagefooter.php"; ?>

