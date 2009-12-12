<?php /* Smarty version 2.6.26, created on 2009-12-11 23:13:34
         compiled from expeditions.tpl */ ?>
<?php $_smarty_tpl_vars = $this->_tpl_vars;
$this->_smarty_include(array('smarty_include_tpl_file' => "header.tpl", 'smarty_include_vars' => array('title' => 'expeditions Page','tab' => 'Expeditions')));
$this->_tpl_vars = $_smarty_tpl_vars;
unset($_smarty_tpl_vars);
 ?>
<h2>All expeditions</h2>
<p><a href="expedition.new">New expedition</a></p>
<div class="expedition_list">
	<?php if ($this->_tpl_vars['expeditions']): ?>
	<?php $_from = $this->_tpl_vars['expeditions']; if (!is_array($_from) && !is_object($_from)) { settype($_from, 'array'); }if (count($_from)):
    foreach ($_from as $this->_tpl_vars['expedition']):
?>
	<div class="list-item">
		<a href="expedition.tracker?id=<?php echo $this->_tpl_vars['expedition']['id']; ?>
"><?php echo $this->_tpl_vars['expedition']['name']; ?>
</a> 
		<p class="description"><?php echo $this->_tpl_vars['expedition']['description']; ?>
</p>
	</div>
	<?php endforeach; endif; unset($_from); ?>
	<?php else: ?>
		<em>There are no expeditions on this project yet.</em>
	<?php endif; ?>
</div>
<?php $_smarty_tpl_vars = $this->_tpl_vars;
$this->_smarty_include(array('smarty_include_tpl_file' => "footer.tpl", 'smarty_include_vars' => array()));
$this->_tpl_vars = $_smarty_tpl_vars;
unset($_smarty_tpl_vars);
 ?>