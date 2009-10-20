<html>
	<head>
		<title>{$title|default:"positweb"}</title>
		<link rel="stylesheet" href="../res/style/basic.css"/>
		<script src="../res/js/util.js"></script>
		<script src="../res/js/jquery-1.3.2.min.js"></script>
	</head>
	<body{$body_attributes}>
		<div id="header">
			<h1><span>Posit</span></h1>
			
			<div id="loginStatus">
				{if $loggedIn}
					Logged in as <strong>{$loginEmail}</strong> [<a href="logout" id="logout-link">log out</a>]
				{else}
					<a href="login">Log in/register</a>
				{/if}
			</div>
			
			<ul id="nav">
				<li{if $tab == "home" || $tab == ""} id="tab-selected"{/if}><a href="main">Home</a></li>
				<li{if $tab == "projects"} id="tab-selected"{/if}><a href="projects">Projects</a></li>
				<li{if $tab == "settings"} id="tab-selected"{/if}><a href="settings">Settings</a></li>
				{if $loginHasAdmin}
					<li{if $tab == "admin"} id="tab-selected"{/if}><a href="admin">Administration</a></li>
				{/if}
			</ul>
		</div>
		<div id="content">