<?php /* Smarty version 2.6.26, created on 2009-06-23 17:09:13
         compiled from find_display.tpl */ ?>
<?php $_smarty_tpl_vars = $this->_tpl_vars;
$this->_smarty_include(array('smarty_include_tpl_file' => "header.tpl", 'smarty_include_vars' => array('title' => $this->_tpl_vars['find']['name'])));
$this->_tpl_vars = $_smarty_tpl_vars;
unset($_smarty_tpl_vars);
 ?>
<h2><?php echo $this->_tpl_vars['find']['name']; ?>
</h2>
<div class="find_details">
	<h3>Description:</h3>
	<div class="find_description"><?php echo $this->_tpl_vars['find']['description']; ?>
</div>
	
	<h3>Project:</h3>
	<div class="find_project"><?php echo $this->_tpl_vars['project']['name']; ?>
</div>
	
	<h3>Time Added:</h3>
	<div class="find_add_time"><?php echo $this->_tpl_vars['find']['add_time']; ?>
</div>
	
	<h3>Location:</h3>
	<div class="find_location">
		Longitude: <?php echo $this->_tpl_vars['find']['longitude']; ?>

		Latitude: <?php echo $this->_tpl_vars['find']['latitude']; ?>

	</div>
	
</div>
<?php $_smarty_tpl_vars = $this->_tpl_vars;
$this->_smarty_include(array('smarty_include_tpl_file' => "footer.tpl", 'smarty_include_vars' => array()));
$this->_tpl_vars = $_smarty_tpl_vars;
unset($_smarty_tpl_vars);
 ?>