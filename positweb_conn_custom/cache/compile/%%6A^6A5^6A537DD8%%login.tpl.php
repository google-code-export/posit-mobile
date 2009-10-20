<?php /* Smarty version 2.6.26, created on 2009-06-24 16:24:15
         compiled from login.tpl */ ?>
<h2>Log in</h2>
<?php if ($this->_tpl_vars['errMsg']): ?>
<div class="error"><?php echo $this->_tpl_vars['errMsg']; ?>
</div>
<?php endif; ?>
<form action="login.do" method="post">
	<input type="hidden" name="doLogin" value="true"/>
	<?php if ($this->_tpl_vars['destination']): ?>
	<input type="hidden" name="loginDestination" value="<?php echo $this->_tpl_vars['destination']; ?>
"/>
	<?php endif; ?>
	<label for="email">Email address:</label><br/>
	<input type="text" id="email" name="loginEmail"/><br/>
	<label for="pass">Password:</label><br/>
	<input type="password" id="pass" name="loginPass"/><br/>
	<input type="submit" value="Log in"/>
</form>
<h2>Register</h2>
<p>Don't have an account? <a href="register">Register one here.</a></p>