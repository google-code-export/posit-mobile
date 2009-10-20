<fieldset>
	<legend>Devices</legend>
	{if count($devices) > 0}
		<table>
			<tr>
				<th>Name</th>
				<th>IMEI</th>
				<th>Added</th>
				<th>Actions</th>
			</tr>
		{foreach from=$devices item=d}
			<tr>
				<td>{if $d.name}{$d.name}{else}--{/if}</td>
				<td>{$d.imei}</td>
				<td>{$d.add_time|date_format}</td>
				<td>[<b><a href="#"
							style="text-decoration: none"
							onClick="if(confirm('Are you sure you want to delete the device \'{$d.name}\' from your account?'))window.location='removeDevice.do?imei={$d.imei}';">X</a></b>]</td>
			</tr>
		{/foreach}
		</table>
	{else}
		There are no devices associated with your account.
	{/if}
	
	<p><a href="settings.registerDevice">Register a device</a></p>
</fieldset>