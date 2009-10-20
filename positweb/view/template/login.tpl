<h2>Log in</h2>
{if $errMsg}
<div class="error">{$errMsg}</div>
{/if}
<form action="login.do" method="post">
	<input type="hidden" name="doLogin" value="true"/>
	{if $destination}
	<input type="hidden" name="loginDestination" value="{$destination}"/>
	{/if}
	<label for="email">Email address:</label><br/>
	<input type="text" id="email" name="loginEmail"/><br/>
	<label for="pass">Password:</label><br/>
	<input type="password" id="pass" name="loginPass"/><br/>
	<input type="submit" value="Log in"/>
</form>
<h2>Register</h2>
<p>Don't have an account? <a href="register">Register one here.</a></p>