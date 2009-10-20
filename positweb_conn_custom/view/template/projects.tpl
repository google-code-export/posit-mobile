{include file="header.tpl" title="Projects Page" tab="projects"}
<h2>All Projects</h2>
<div class="project_list">
	{foreach from=$projects item=project}
		<a href="project.display?id={$project.id}">{$project.name}</a><br/>
	{/foreach}
</div>
{include file="footer.tpl"}