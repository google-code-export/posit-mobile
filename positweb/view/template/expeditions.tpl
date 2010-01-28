{include file="header.tpl" title="expeditions Page" tab="Expeditions"}
<h2>All expeditions</h2>
<p><a href="expedition.new">New expedition</a></p>
<div class="expedition_list">
	{if $expeditions}
	{foreach from=$expeditions item=expedition}
	<div class="list-item">
                <a href="expedition.tracker?id={$expedition.id}">{$expedition.name} {$expedition.id}</a> 
		<p class="description">{$expedition.description}</p>
	</div>
	{/foreach}
	{else}
		<em>There are no expeditions on this project yet.</em>
	{/if}
</div>
{include file="footer.tpl"}