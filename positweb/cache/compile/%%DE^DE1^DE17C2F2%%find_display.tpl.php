<?php /* Smarty version 2.6.26, created on 2010-01-05 21:33:53
         compiled from find_display.tpl */ ?>
<?php $_smarty_tpl_vars = $this->_tpl_vars;
$this->_smarty_include(array('smarty_include_tpl_file' => "header.tpl", 'smarty_include_vars' => array('title' => $this->_tpl_vars['find']['name'],'tab' => 'projects')));
$this->_tpl_vars = $_smarty_tpl_vars;
unset($_smarty_tpl_vars);
 ?>
<h2><?php echo $this->_tpl_vars['find']['name']; ?>
</h2>
<p><a href="project.display?id=<?php echo $this->_tpl_vars['find']['project_id']; ?>
">All Finds</a></p>
<div class="find_details">
	<h3>Description:</h3>
	<div class="find_description"><?php echo $this->_tpl_vars['find']['description']; ?>
</div>
	
	<h3>Project:</h3>
	<div class="find_project"><?php echo $this->_tpl_vars['project']['name']; ?>
</div>
	
	<h3>Time Added:</h3>
	<div class="find_add_time"><?php echo $this->_tpl_vars['find']['add_time']; ?>
</div>
	<h3> Barcode:</h3>
	<div class="associated_barcode"><?php echo $this->_tpl_vars['find']['barcode_id']; ?>
</div>
	<h3>Location:</h3>
	<div class="find_location">
		Longitude: <?php echo $this->_tpl_vars['find']['longitude']; ?>

		Latitude: <?php echo $this->_tpl_vars['find']['latitude']; ?>

	</div>
	<div class="map-display">
	<img src="http://maps.google.com/maps/api/staticmap?zoom=14&size=256x256&markers=color:red|<?php echo $this->_tpl_vars['find']['latitude']; ?>
,<?php echo $this->_tpl_vars['find']['longitude']; ?>
&sensor=false&key=<?php echo @GOOGLE_MAPS_KEY; ?>
"/>
	
	</div>
	
	<div class="picture_loop">
	<?php $_from = $this->_tpl_vars['images']; if (!is_array($_from) && !is_object($_from)) { settype($_from, 'array'); }if (count($_from)):
    foreach ($_from as $this->_tpl_vars['imageid']):
?>
	<img src="displayPicture?id=<?php echo $this->_tpl_vars['imageid']; ?>
&size=full"/>
	<?php endforeach; endif; unset($_from); ?>
	</div>
	
	<br />
	
	<div class="video_loop">
		<?php $_from = $this->_tpl_vars['videos']; if (!is_array($_from) && !is_object($_from)) { settype($_from, 'array'); }if (count($_from)):
    foreach ($_from as $this->_tpl_vars['videoid']):
?>
			<a href="displayVideo?id=<?php echo $this->_tpl_vars['videoid']; ?>
">Click to download video</a>
		<?php endforeach; endif; unset($_from); ?>
	</div>
	
	<div class="audio_loop">
		<?php $_from = $this->_tpl_vars['audios']; if (!is_array($_from) && !is_object($_from)) { settype($_from, 'array'); }if (count($_from)):
    foreach ($_from as $this->_tpl_vars['audioid']):
?>
			<embed type="application/x-shockwave-flash" 
				src="http://www.google.com/reader/ui/3247397568-audio-player.swf?audioUrl=displayAudio?id=<?php echo $this->_tpl_vars['audioid']; ?>
" 
				width="400" height="27" allowscriptaccess="never" quality="best" bgcolor="#ffffff" wmode="window" 
				flashvars="playerMode=embedded"
			/> <br />
		<?php endforeach; endif; unset($_from); ?>
	</div>
	
</div>
<?php $_smarty_tpl_vars = $this->_tpl_vars;
$this->_smarty_include(array('smarty_include_tpl_file' => "footer.tpl", 'smarty_include_vars' => array()));
$this->_tpl_vars = $_smarty_tpl_vars;
unset($_smarty_tpl_vars);
 ?>