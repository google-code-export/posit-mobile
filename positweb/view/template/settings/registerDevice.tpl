{include file="header.tpl" title="Register a Device" body_attributes=" onLoad=\"startCheckingDeviceRegistration('$baseUrl', '$authKey')\"" tab="settings"}
<h2>Settings</h2>
<h3>Register a device</h3>
<div id="registration-code">
	<p>With POSIT open on your device, go to <strong>Settings > Server Registration</strong> and scan this code:</p>
	<p style="text-align: center"><img src="qrcode?d={$data}"/></p>
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
{include file="footer.tpl"}