<?php /* Smarty version 2.6.26, created on 2010-01-15 11:22:41
         compiled from expedition_tracker.tpl */ ?>
<?php $_smarty_tpl_vars = $this->_tpl_vars;
$this->_smarty_include(array('smarty_include_tpl_file' => "header.tpl", 'smarty_include_vars' => array('title' => $this->_tpl_vars['expedition']['name'],'tab' => 'Expeditions')));
$this->_tpl_vars = $_smarty_tpl_vars;
unset($_smarty_tpl_vars);
 ?>
<!--<script src="http://maps.google.com/maps?file=api&amp;v=2&amp;sensor=false&amp;-->
<!--key=<?php echo @GOOGLE_MAPS_KEY; ?>
"/>-->
<?php echo '
<script type="text/javascript"> 
google.load("maps", "2.x");

    function initialize() {
      
        var map = new google.maps.Map2(document.getElementById("map_canvas"));
        var polyline = new GPolyline([
        '; ?>

        	<?php $_from = $this->_tpl_vars['expeditionPoints']; if (!is_array($_from) && !is_object($_from)) { settype($_from, 'array'); }if (count($_from)):
    foreach ($_from as $this->_tpl_vars['expeditionPoint']):
?>

        	 new GLatLng(<?php echo $this->_tpl_vars['expeditionPoint']['latitude']; ?>
,<?php echo $this->_tpl_vars['expeditionPoint']['longitude']; ?>
),
        	<?php endforeach; endif; unset($_from); ?>
         ], "#ff0000", 10);  	  
        <?php echo '  
        map.addOverlay(polyline);
            	
        map.setUIToDefault();
    
    }
        	   google.setOnLoadCallback(initialize);
        	   
    </script> 
    '; ?>

    <div id="map_canvas" style="width: 500px; height: 300px"></div> 

<?php $_smarty_tpl_vars = $this->_tpl_vars;
$this->_smarty_include(array('smarty_include_tpl_file' => "footer.tpl", 'smarty_include_vars' => array()));
$this->_tpl_vars = $_smarty_tpl_vars;
unset($_smarty_tpl_vars);
 ?>