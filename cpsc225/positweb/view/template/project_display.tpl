{include file="header.tpl" title=$project.name tab="projects"}
<h2>{$project.name}</h2>
<div class="project_finds">
	{foreach from=$finds item=find}
		<div class="find_name"><a href="find.display?id={$find.id}">{$find.name}&#8212;{$find.add_time|date_format:"%B %e, %Y %I:%M:%S"}</a></div>
		<div class="find_description">{$find.description}</div>
	{/foreach}
</div>
{include file="footer.tpl"}