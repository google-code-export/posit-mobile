<?php /* Smarty version 2.6.26, created on 2009-06-23 11:23:20
         compiled from register.tpl */ ?>
<?php $_smarty_tpl_vars = $this->_tpl_vars;
$this->_smarty_include(array('smarty_include_tpl_file' => "header.tpl", 'smarty_include_vars' => array('title' => 'Register')));
$this->_tpl_vars = $_smarty_tpl_vars;
unset($_smarty_tpl_vars);
 ?>
<h2>Register</h2>
<form action="register.do" method="post">
	<table>
		<tr>
			<th><label for="firstName">First name:</label></th>
			<td><input type="text" name="firstName" id="firstName"/></td>
		</tr>
		<tr>
			<th><label for="lastName">Last name:</label></th>
			<td><input type="text" name="lastName" id="lastName"/></td>
		</tr>
		<tr>
			<th><label for="email">Email:</label></th>
			<td><input type="text" name="email" id="email"/></td>
		</tr>
		<tr>
			<th><label for="pass1">Choose a password:</label></th>
			<td>
				<input type="password" name="pass1" id="pass1"/>
			</td>
		</tr>
		<tr>
			<th><label for="pass2">Confirm password:</label></th>
			<td>
				<input type="password" name="pass2" id="pass2"/>
			</td>
		</tr>
		<tr>
			<td colspan="2" style="text-align: right">
				<input type="submit" value="Register"/>
			</td>
	</table>
</form>
<?php $_smarty_tpl_vars = $this->_tpl_vars;
$this->_smarty_include(array('smarty_include_tpl_file' => "footer.tpl", 'smarty_include_vars' => array()));
$this->_tpl_vars = $_smarty_tpl_vars;
unset($_smarty_tpl_vars);
 ?>