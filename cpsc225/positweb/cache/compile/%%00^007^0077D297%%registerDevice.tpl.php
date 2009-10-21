<?php /* Smarty version 2.6.26, created on 2009-07-14 08:41:38
         compiled from settings/registerDevice.tpl */ ?>
<?php $_smarty_tpl_vars = $this->_tpl_vars;
$this->_smarty_include(array('smarty_include_tpl_file' => "header.tpl", 'smarty_include_vars' => array('title' => 'Register a Device','body_attributes' => " onLoad=\"startCheckingDeviceRegistration('".($this->_tpl_vars['baseUrl'])."', '".($this->_tpl_vars['authKey'])."')\"",'tab' => 'settings')));
$this->_tpl_vars = $_smarty_tpl_vars;
unset($_smarty_tpl_vars);
 ?>
<h2>Settings</h2>
<h3>Register a device</h3>
<div id="registration-code">
	<p>With POSIT open on your device, go to <strong>Settings > Server Registration</strong> and scan this code:</p>
	<p style="text-align: center"><img src="../3rd/qr_img.php?d=<?php echo $this->_tpl_vars['data']; ?>
"/></p>
</div>
<div id="flash"><img src="../res/image/greencheck.png"/></div>
<div id="registration-device-name" style="display: none">
	<form action="settings.registerDevice.do" method="post">
		<input type="hidden" name="imei" id="imei-hidden" value=""/>
		<p>Detected device with IMEI# <strong id="device-imei"></strong>.</p>
		<p>
			<label for="deviceName">Enter a nickname for this device (optional):</label>
			<input type="text" name="deviceName" id="deviceName"/>
			<input type="submit" value="Register"/>
		</p>
	</form>
</div>

	<p><a href="settings">Cancel</a></p>
<?php $_smarty_tpl_vars = $this->_tpl_vars;
$this->_smarty_include(array('smarty_include_tpl_file' => "footer.tpl", 'smarty_include_vars' => array()));
$this->_tpl_vars = $_smarty_tpl_vars;
unset($_smarty_tpl_vars);
 ?>