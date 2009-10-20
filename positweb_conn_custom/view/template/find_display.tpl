{include file="header.tpl" title=$find.name tab="projects"}
<h2>{$find.name}</h2>
<div class="find_details">
	<h3>Description:</h3>
	<div class="find_description">{$find.description}</div>
	
	<h3>Project:</h3>
	<div class="find_project">{$project.name}</div>
	
	<h3>Time Added:</h3>
	<div class="find_add_time">{$find.add_time}</div>
	
	<h3>Location:</h3>
	<div class="find_location">
		Longitude: {$find.longitude}
		Latitude: {$find.latitude}
	</div>
	
	<div class="picture_loop">
	{foreach from=$images item=imageid}
	<img src="displayPicture?id={$imageid}&size=full"/>
	{/foreach}
	</div>
	
</div>
{include file="footer.tpl"}