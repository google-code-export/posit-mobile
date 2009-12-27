{include file="header.tpl" title="New Instance" tab="projects"}
<h2>{$project.name}</h2>
<div>
	<form action="customPosit.create.do" method="post">
			<input type="hidden" name="project_id" value="{$project_id}"/>
		<p>
			<label for="name">Instance name:</label><br/>
			<input type="text" name="name"/>
		</p>
		<p>
			<label for="description">Description: <span style="color: #666">(optional)</span></label><br/>
			<textarea rows="6" cols="30" name="description"></textarea>
		</p>
		<p>
			<label for="sync_on">Enable Syncing:</label><br/>
			Yes<INPUT TYPE="RADIO" NAME="syncOn" checked>
			No <INPUT TYPE="RADIO" NAME="syncOn"> </br>
		</p>
		
		<p>
			<label for="device_id">Enable Device:</label><br/>
			<select name="device_id">
			{foreach from=$devices item=device}
			<option value={$smarty.foreach.device.index}>{$device.imei}</option>
			{/foreach}
			</select>
			
		</p>
		
		<input type="submit" value="Create Instance"/>	
	</form>
</div>
{include file="footer.tpl"}
