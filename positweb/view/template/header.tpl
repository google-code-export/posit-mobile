<html>
	<head>
		<title>{$title|default:"positweb"}</title>
		<link rel="stylesheet" href="../res/style/basic.css"/>
		<script src="../res/js/util.js"></script>
		<script src="../res/js/jquery-1.3.2.min.js"></script>
		<script type="text/javascript" src="http://www.google.com/jsapi?key={$smarty.const.GOOGLE_MAPS_KEY}"></script>
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
				<li{if $tab == "maps"} id="tab-selected"{/if}><a href="maps">Maps</a></li>
				{if $loginHasAdmin}
					<li{if $tab == "admin"} id="tab-selected"{/if}><a href="admin">Administration</a></li>
				{/if}
				<li{if $tab == "Expeditions"} id="tab-selected"{/if}><a href="expeditions">Expeditions</a></li>
			</ul>
		</div>
		<div id="content">
