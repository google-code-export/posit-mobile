<?php /* Smarty version 2.6.26, created on 2010-01-15 10:41:25
         compiled from project_mapdisplay.tpl */ ?>
<?php $_smarty_tpl_vars = $this->_tpl_vars;
$this->_smarty_include(array('smarty_include_tpl_file' => "header.tpl", 'smarty_include_vars' => array('title' => $this->_tpl_vars['project']['name'],'tab' => 'maps')));
$this->_tpl_vars = $_smarty_tpl_vars;
unset($_smarty_tpl_vars);
 ?>
<h2><?php echo $this->_tpl_vars['project']['name']; ?>
</h2>
    <script>
    <?php echo '
    	function sum(a) {
    		var sum = 0;
    		for(var i in a)
    			sum += a[i];
    		return sum;
    	}
    	
    	function avg(a) {
    		if(a.length > 0)
    			return sum(a)/a.length;
    		else
    			return 0;
    	}
    	
    	function min(a) {
    		var min = null;
    		for(var i in a) {
    			if(min == null || a[i] < min)
    				min = a[i];
    		}
    		return min;
    	}
    	
    	function max(a) {
    		var max = null;
    		for(var i in a) {
    			if(max == null || a[i] > max)
    				max = a[i];
    		}
    		return max;
    	}
    	
    	function distanceInMiles(lat1, lng1, lat2, lng2) {
    		return (3958.75 * Math.acos(Math.sin(lat1 / 57.2958) * Math.sin(lat2 / 57.2958) + Math.cos(lat1 / 57.2958) * Math.cos(lat2 / 57.2958) * Math.cos(lng2 / 57.2958 - lng1 / 57.2958))); 
    	}
    
    '; ?>

    
   <?php echo '
   google.load("maps", "2.x");
   // Call this function when the page has been loaded
   function initialize() {
     '; ?>

     var location_data = eval('<?php echo $this->_tpl_vars['location_data']; ?>
');
     var finds = eval('<?php echo $this->_tpl_vars['finds']; ?>
');
     <?php echo '
     var map = new google.maps.Map2(document.getElementById("map"));
     
     var markers = [];
     var lats = [];
     var lngs = [];
     
var redIcon = new GIcon(G_DEFAULT_ICON);
redIcon.image = "http://posit.hfoss.org/demo/res/image/redmark.png";
redIcon.iconSize = new GSize(14, 22);
redIcon.iconAnchor = new GPoint(7, 21);
redIcon.shadow = "http://posit.hfoss.org/demo/res/image/redshadow.png";
redIcon.shadowSize = new GSize(39, 31);
                
// Set up our GMarkerOptions object
//markerOptions = { icon:redIcon };
     
     
     for(var i in finds) {
     	var point = new GLatLng(finds[i][\'latitude\'], finds[i][\'longitude\']);
     	var marker = new GMarker(point);
     	marker.findIndex = i;
     	GEvent.addListener(marker, "click", function() {
			this.openInfoWindowHtml("<b>"+ finds[this.findIndex][\'name\'] + "</b><p>" + finds[this.findIndex][\'description\'] + \'</p><p><a target="find" href="find.display?id=\' + finds[this.findIndex][\'id\'] + \'">more info</a></p>\');
		});
     	
     	
     	markers.push(marker);
     	lats.push(point.lat());
     	lngs.push(point.lng());
     	
     	map.addOverlay(marker);
     }
     
     var center = new GLatLng(avg([max(lats), min(lats)]),avg([max(lngs), min(lngs)]));
     var pointSpan = max([
     	distanceInMiles(max(lats), max(lngs), min(lats), max(lngs)),
     	distanceInMiles(max(lats), max(lngs), max(lats), min(lngs))
     ]);
     
     var zoomLevel = null;
     
     		if(pointSpan < 0.2) zoomLevel = 16;
     else	if(pointSpan < 0.5) zoomLevel = 15;
     else	if(pointSpan <   1) zoomLevel = 14;
     else	if(pointSpan <   2) zoomLevel = 13;
     else	if(pointSpan <   3) zoomLevel = 12;
     else	if(pointSpan <   7) zoomLevel = 11;
     else	if(pointSpan <  15) zoomLevel = 10;
     else						zoomLevel =  9;
     
     map.setCenter(center, zoomLevel + 1);
     
    var mapControl = new GMapTypeControl();
	map.addControl(mapControl);
	map.addControl(new GLargeMapControl());
	
	var pointSW = new GLatLng(41.7424929762,-72.6934718466);
	var pointNE = new GLatLng(41.7512994385,-72.687009635);

	var groundOverlay = new GGroundOverlay(
   "http://dev.posit.ossf.org/tracker/tracker_2009-7-16-01.png", 
   new GLatLngBounds(pointSW, pointNE));

map.addOverlay(groundOverlay);

	pointSW = new GLatLng(41.743258835, -72.692003249);
	pointNE = new GLatLng(41.7448794831, -72.6873892557);
	var groundOverlay2 = new GGroundOverlay(
	"http://www.cs.trincoll.edu/~aalcorn/posit/media/path2a.png",
	new GLatLngBounds(pointSW, pointNE)
	);
	map.addOverlay(groundOverlay2);
	
    }  
   google.setOnLoadCallback(initialize);
   '; ?>

   </script>
   <body>
   <div id="map" style="width: 500px; height: 400px;"></div>
    <div id="searchcontrol"></div>
   </body>
<?php $_smarty_tpl_vars = $this->_tpl_vars;
$this->_smarty_include(array('smarty_include_tpl_file' => "footer.tpl", 'smarty_include_vars' => array()));
$this->_tpl_vars = $_smarty_tpl_vars;
unset($_smarty_tpl_vars);
 ?>