{include file="header.tpl" title="Projects Page" tab="projects"}
<h2>All Projects</h2>
<div class="project_list">
	{if $projects}
	{foreach from=$projects item=project}
		<a href="project.display?id={$project.id}">{$project.name}</a><br/>
	{/foreach}
	{else}
		<em>There are no projects on this server yet.</em>
	{/if}
</div>
<p><a href="project.new">New project</a></p>
{include file="footer.tpl"}