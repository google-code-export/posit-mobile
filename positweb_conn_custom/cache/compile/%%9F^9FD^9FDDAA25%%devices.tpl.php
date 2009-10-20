<?php /* Smarty version 2.6.26, created on 2009-06-26 10:38:46
         compiled from settings/devices.tpl */ ?>
<?php require_once(SMARTY_CORE_DIR . 'core.load_plugins.php');
smarty_core_load_plugins(array('plugins' => array(array('modifier', 'date_format', 'settings/devices.tpl', 15, false),)), $this); ?>
<fieldset>
	<legend>Devices</legend>
	<?php if (count ( $this->_tpl_vars['devices'] ) > 0): ?>
		<table>
			<tr>
				<th>Name</th>
				<th>IMEI</th>
				<th>Added</th>
				<th>Actions</th>
			</tr>
		<?php $_from = $this->_tpl_vars['devices']; if (!is_array($_from) && !is_object($_from)) { settype($_from, 'array'); }if (count($_from)):
    foreach ($_from as $this->_tpl_vars['d']):
?>
			<tr>
				<td><?php if ($this->_tpl_vars['d']['name']): ?><?php echo $this->_tpl_vars['d']['name']; ?>
<?php else: ?>--<?php endif; ?></td>
				<td><?php echo $this->_tpl_vars['d']['imei']; ?>
</td>
				<td><?php echo ((is_array($_tmp=$this->_tpl_vars['d']['add_time'])) ? $this->_run_mod_handler('date_format', true, $_tmp) : smarty_modifier_date_format($_tmp)); ?>
</td>
				<td>[<b><a href="#"
							style="text-decoration: none"
							onClick="if(confirm('Are you sure you want to delete the device \'<?php echo $this->_tpl_vars['d']['name']; ?>
\' from your account?'))window.location='removeDevice.do?imei=<?php echo $this->_tpl_vars['d']['imei']; ?>
';">X</a></b>]</td>
			</tr>
		<?php endforeach; endif; unset($_from); ?>
		</table>
	<?php else: ?>
		There are no devices associated with your account.
	<?php endif; ?>
	
	<p><a href="settings.registerDevice">Register a device</a></p>
</fieldset>