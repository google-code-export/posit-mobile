<?php /* Smarty version 2.6.26, created on 2009-12-11 03:24:31
         compiled from new_project.tpl */ ?>
<?php $_smarty_tpl_vars = $this->_tpl_vars;
$this->_smarty_include(array('smarty_include_tpl_file' => "header.tpl", 'smarty_include_vars' => array('title' => 'New Project','tab' => 'projects')));
$this->_tpl_vars = $_smarty_tpl_vars;
unset($_smarty_tpl_vars);
 ?>
<h2><?php echo $this->_tpl_vars['project']['name']; ?>
</h2>
<div>
	<form action="project.new.do" method="post">
		<p>
			<label for="name">Project name:</label><br/>
			<input type="text" name="name"/>
		</p>
		
		<p>
			<label for="description">Description: <span style="color: #666">(optional)</span></label><br/>
			<textarea rows="6" cols="30" name="description"></textarea>
		</p>
		<input type="submit" value="Add project"/>	
	</form>
</div>
<?php $_smarty_tpl_vars = $this->_tpl_vars;
$this->_smarty_include(array('smarty_include_tpl_file' => "footer.tpl", 'smarty_include_vars' => array()));
$this->_tpl_vars = $_smarty_tpl_vars;
unset($_smarty_tpl_vars);
 ?>