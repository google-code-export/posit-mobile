<?php /* Smarty version 2.6.26, created on 2009-06-26 10:38:46
         compiled from header.tpl */ ?>
<?php require_once(SMARTY_CORE_DIR . 'core.load_plugins.php');
smarty_core_load_plugins(array('plugins' => array(array('modifier', 'default', 'header.tpl', 3, false),)), $this); ?>
<html>
	<head>
		<title><?php echo ((is_array($_tmp=@$this->_tpl_vars['title'])) ? $this->_run_mod_handler('default', true, $_tmp, 'positweb') : smarty_modifier_default($_tmp, 'positweb')); ?>
</title>
		<link rel="stylesheet" href="../res/style/basic.css"/>
		<script src="../res/js/util.js"></script>
		<script src="../res/js/jquery-1.3.2.min.js"></script>
	</head>
	<body<?php echo $this->_tpl_vars['body_attributes']; ?>
>
		<div id="header">
			<h1><span>Posit</span></h1>
			
			<div id="loginStatus">
				<?php if ($this->_tpl_vars['loggedIn']): ?>
					Logged in as <strong><?php echo $this->_tpl_vars['loginEmail']; ?>
</strong> [<a href="logout" id="logout-link">log out</a>]
				<?php else: ?>
					<a href="login">Log in/register</a>
				<?php endif; ?>
			</div>
			
			<ul id="nav">
				<li<?php if ($this->_tpl_vars['tab'] == 'home' || $this->_tpl_vars['tab'] == ""): ?> id="tab-selected"<?php endif; ?>><a href="main">Home</a></li>
				<li<?php if ($this->_tpl_vars['tab'] == 'projects'): ?> id="tab-selected"<?php endif; ?>><a href="projects">Projects</a></li>
				<li<?php if ($this->_tpl_vars['tab'] == 'settings'): ?> id="tab-selected"<?php endif; ?>><a href="settings">Settings</a></li>
				<?php if ($this->_tpl_vars['loginHasAdmin']): ?>
					<li<?php if ($this->_tpl_vars['tab'] == 'admin'): ?> id="tab-selected"<?php endif; ?>><a href="admin">Administration</a></li>
				<?php endif; ?>
			</ul>
		</div>
		<div id="content">