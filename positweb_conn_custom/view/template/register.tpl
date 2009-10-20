{include file="header.tpl" title="Register"}
<h2>Register</h2>
<form action="register.do" method="post" id="registration-form">
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
{include file="footer.tpl"}