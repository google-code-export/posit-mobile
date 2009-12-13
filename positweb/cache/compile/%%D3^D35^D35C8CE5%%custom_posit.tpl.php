<?php /* Smarty version 2.6.26, created on 2009-12-13 14:26:33
         compiled from custom_posit.tpl */ ?>
<?php $_smarty_tpl_vars = $this->_tpl_vars;
$this->_smarty_include(array('smarty_include_tpl_file' => "header.tpl", 'smarty_include_vars' => array('title' => 'New Instance','tab' => 'projects')));
$this->_tpl_vars = $_smarty_tpl_vars;
unset($_smarty_tpl_vars);
 ?>
<h2><?php echo $this->_tpl_vars['project']['name']; ?>
</h2>
<div>
	<form action="customPosit.new.do" method="post">
		<p>
			<label for="name">Instance name:</label><br/>
			<input type="text" name="name"/>
		</p>
		<p>
			<label for="description">Description: <span style="color: #666">(optional)</span></label><br/>
			<textarea rows="6" cols="30" name="description"></textarea>
		</p>
		<p>
			<label for="syncOn">Enable Syncing:</label><br/>
			Yes<INPUT TYPE="RADIO" NAME="syncOn" checked>
			No <INPUT TYPE="RADIO" NAME="syncOn"> </br>
		</p>
		
		<p>
			<label for="deviceId">Enable Device:</label><br/>
			<select name="deviceId">
			<?php $_from = $this->_tpl_vars['devices']; if (!is_array($_from) && !is_object($_from)) { settype($_from, 'array'); }if (count($_from)):
    foreach ($_from as $this->_tpl_vars['device']):
?>
			<option value=<?php echo ($this->_foreach['device']['iteration']-1); ?>
><?php echo $this->_tpl_vars['device']['imei']; ?>
</option>
			<?php endforeach; endif; unset($_from); ?>
			</select>
			
		</p>
		
		<input type="submit" value="Create Instance"/>	
	</form>
</div>
<?php $_smarty_tpl_vars = $this->_tpl_vars;
$this->_smarty_include(array('smarty_include_tpl_file' => "footer.tpl", 'smarty_include_vars' => array()));
$this->_tpl_vars = $_smarty_tpl_vars;
unset($_smarty_tpl_vars);
 ?>