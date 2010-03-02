{include file="header.tpl" title=$find.name tab="projects"}
<p><a href="project.display?id={$find.project_id}">All Finds</a></p>
<h2>Name: {$find.name}</h2>
<div class="find_details">
<!-- 	<div class="find_img"><IMG src={$find.img} width="40" height="40"></div>  -->
	<p><b>GUID: </b>{$find.barcode_id}
	<br><b>Description: </b>{$find.description}
	<br><b>Project: </b>{$project.name}
	<br><b>Time Added: </b>{$find.add_time}
	<br><b>Location: </b> [Longitude={$find.longitude},  Latitude={$find.latitude}]

<!-- 	<div class="find_description">{$find.description}</div> 
	
	<h3>Project:
	<div class="find_project">{$project.name}</div></h3>
	
	<h3>Time Added:</h3>
	<div class="find_add_time">{$find.add_time}</div>
	<h3> GUID:</h3>
	<div class="associated_barcode">{$find.barcode_id}</div>
	<h3>Location:</h3>
	<div class="find_location">
		Longitude: {$find.longitude}
		Latitude: {$find.latitude}
	</div>
--------  -->
	<div class="map-display">
	<img src="http://maps.google.com/maps/api/staticmap?zoom=14&size=256x256&markers=color:red|{$find.latitude},{$find.longitude}&sensor=false&key={$smarty.const.GOOGLE_MAPS_KEY}"/>
	
	</div>
	
	<div class="picture_loop">
	{foreach from=$images item=imageid}
	<img src="displayPicture?id={$imageid}&size=full"/>
	{/foreach}
	</div>
	
	<br />
	
	<div class="video_loop">
		{foreach from=$videos item=videoid}
			<a href="displayVideo?id={$videoid}">Click to download video</a>
		{/foreach}
	</div>
	
	<div class="audio_loop">
		{foreach from=$audios item=audioid}
			<embed type="application/x-shockwave-flash" 
				src="http://www.google.com/reader/ui/3247397568-audio-player.swf?audioUrl=displayAudio?id={$audioid}" 
				width="400" height="27" allowscriptaccess="never" quality="best" bgcolor="#ffffff" wmode="window" 
				flashvars="playerMode=embedded"
			/> <br />
		{/foreach}
	</div>
	
</div>
{include file="footer.tpl"}
