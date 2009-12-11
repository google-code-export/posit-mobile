{include file="header.tpl" title="New Project" tab="projects"}
<h2>{$project.name}</h2>
<div>
	<form action="project.new.do" method="post">
		<p>
			<label for="name">Project name:</label><br/>
			<input type="text" name="name"/>
		</p>
		
		<p>
			<label for="description">Description: <span style="color: #666">(optional)</span></label><br/>
			<textarea rows="6" cols="30" name="description"></textarea>
		</p>
		<input type="submit" value="Add project"/>	
	</form>
</div>
{include file="footer.tpl"}