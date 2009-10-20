function flashGreen() {
	$("#flash").fadeIn(500);
	$("#flash").fadeOut(500);
}


var xmlhttp;
var foundRegistration = false;

function startCheckingDeviceRegistration(baseUrl, authKey) {
	xmlhttp=null;
	if(window.XMLHttpRequest)
		xmlhttp=new XMLHttpRequest();
	else if (window.ActiveXObject)
		xmlhttp=new ActiveXObject("Microsoft.XMLHTTP");
	setInterval('checkDeviceRegistration(\''+baseUrl+'\', \''+authKey+'\')', 500);
}

function checkDeviceRegistration(baseUrl, authKey) {
	if(xmlhttp!=null) {
		xmlhttp.onreadystatechange=handleRegistrationData();
		xmlhttp.open("GET", baseUrl + '/api/getPendingDeviceStatus?authKey=' + authKey, true);
		xmlhttp.send(null);
	}
	else
		alert("Your browser does not support XMLHTTP.");
}

function handleRegistrationData() {
	if(xmlhttp.readyState==4) {
		if(xmlhttp.status==200) {
			var device = eval("("+xmlhttp.responseText+")");
			if(device != false && device != "false" && !foundRegistration) {
				flashGreen();
				document.getElementById('registration-code').style.display = 'none';
				document.getElementById('registration-device-name').style.display = 'block';
				document.getElementById('device-imei').innerHTML =  device.imei;
				document.getElementById('imei-hidden').value = device.imei;
				foundRegistration = true;
			}
	    }
	else
		;
	}
}