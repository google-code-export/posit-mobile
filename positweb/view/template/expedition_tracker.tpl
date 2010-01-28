{include file="header.tpl" title=$expedition.name tab="Expeditions"}
<!--<script src="http://maps.google.com/maps?file=api&amp;v=2&amp;sensor=false&amp;-->
<!--key={$smarty.const.GOOGLE_MAPS_KEY}"/>-->
{literal}
<script type="text/javascript"> 
google.load("maps", "2.x");

    function initialize() {
      
        var map = new google.maps.Map2(document.getElementById("map_canvas"));
        var polyline = new GPolyline([
        {/literal}
        	{foreach from=$expeditionPoints item=expeditionPoint}

        	 new GLatLng({$expeditionPoint.latitude},{$expeditionPoint.longitude}),
        	{/foreach}
         ], "#ff0000", 10);  	  
        {literal}  
        map.addOverlay(polyline);
        map.setUIToDefault();
        map.setCenter(new GLatLng(41.71277, -72.67099), 10);
    }
 	   google.setOnLoadCallback(initialize);
        	   
    </script> 
    {/literal}
    <div id="map_canvas" style="width: 500px; height: 300px"></div> 

{include file="footer.tpl"}