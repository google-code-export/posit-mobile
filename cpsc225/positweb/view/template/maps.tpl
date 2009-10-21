{include file="header.tpl" title="Maps" tab="maps"}
<h2>maps</h2>
<div class="project_list">
	{foreach from=$projects item=project}
		<a href="project.mapdisplay?id={$project.id}">{$project.name}</a><br/>
	{/foreach}
</div>
{include file="footer.tpl"}