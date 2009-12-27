{include file="header.tpl" title="Instances" tab="projects"}
<h2> instances </h2>
<div class="list-item">
<div class="instance_name"><a href="customPosit.download?project_id={$project_id}">Download Instance For Project</a></div>
</div>
{* Ignore this for now
{foreach from=$instances item=instance}
<div class="list-item">
	<div class="instance_name"><a href="customPosit.download?project_id={$project_id}">{$instance.name}</div>
</div>
{/foreach}
*}
<a href="customPosit?project_id={$project_id}">Create New One</a>
{include file="footer.tpl"}
