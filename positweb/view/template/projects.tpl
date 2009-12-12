{include file="header.tpl" title="Projects Page" tab="projects"}
<h2>All Projects</h2>
<p><a href="project.new">New project</a></p>
<div class="project_list">
	{if $projects}
	{foreach from=$projects item=project}
	<div class="list-item">
		<a href="project.display?id={$project.id}">{$project.name}</a> 
		<p class="description">{$project.description}</p>
	</div>
	{/foreach}
	{else}
		<em>There are no projects on this server yet.</em>
	{/if}
</div>
{include file="footer.tpl"}