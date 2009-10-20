<?php /* Smarty version 2.6.26, created on 2009-06-23 17:09:08
         compiled from project_display.tpl */ ?>
<?php require_once(SMARTY_CORE_DIR . 'core.load_plugins.php');
smarty_core_load_plugins(array('plugins' => array(array('modifier', 'date_format', 'project_display.tpl', 5, false),)), $this); ?>
<?php $_smarty_tpl_vars = $this->_tpl_vars;
$this->_smarty_include(array('smarty_include_tpl_file' => "header.tpl", 'smarty_include_vars' => array('title' => $this->_tpl_vars['project']['name'])));
$this->_tpl_vars = $_smarty_tpl_vars;
unset($_smarty_tpl_vars);
 ?>
<h2><?php echo $this->_tpl_vars['project']['name']; ?>
</h2>
<div class="project_finds">
	<?php $_from = $this->_tpl_vars['finds']; if (!is_array($_from) && !is_object($_from)) { settype($_from, 'array'); }if (count($_from)):
    foreach ($_from as $this->_tpl_vars['find']):
?>
		<div class="find_name"><a href="find.display?id=<?php echo $this->_tpl_vars['find']['id']; ?>
"><?php echo $this->_tpl_vars['find']['name']; ?>
&#8212;<?php echo ((is_array($_tmp=$this->_tpl_vars['find']['add_time'])) ? $this->_run_mod_handler('date_format', true, $_tmp, "%B %e, %Y %I:%M:%S") : smarty_modifier_date_format($_tmp, "%B %e, %Y %I:%M:%S")); ?>
</a></div>
		<div class="find_description"><?php echo $this->_tpl_vars['find']['description']; ?>
</div>
	<?php endforeach; endif; unset($_from); ?>
</div>
<?php $_smarty_tpl_vars = $this->_tpl_vars;
$this->_smarty_include(array('smarty_include_tpl_file' => "footer.tpl", 'smarty_include_vars' => array()));
$this->_tpl_vars = $_smarty_tpl_vars;
unset($_smarty_tpl_vars);
 ?>