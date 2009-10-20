{include file="header.tpl" title=$project.name tab="maps"}
<h2>{$project.name}</h2>
    <script>
   {literal}
   google.load("maps", "2.x");
   // Call this function when the page has been loaded
   function initialize();
     {/literal}
     var location_data = eval('{$location_data}');
     var map = new google.maps.Map2(document.getElementById("map"));
     map.setCenter(new google.maps.LatLng(37.4419, -122.1419), 13);
     
     // Add 10 markers to the map at random locations
      var bounds = map.getBounds();
      var southWest = bounds.getSouthWest();
      var northEast = bounds.getNorthEast();
      var lngSpan = northEast.lng() - southWest.lng();
      var latSpan = northEast.lat() - southWest.lat();
      for (var i = 0; i < 10; i++) {
        var point = new GLatLng(southWest.lat() + latSpan * Math.random(),
        southWest.lng() + lngSpan * Math.random());
      map.addOverlay(new GMarker(point));
     
      }
    }  
   google.setOnLoadCallback(initialize);
   {/literal}
   </script>
   <body>
   <div id="map" style="width: 500px; height: 600px;"></div>
    <div id="searchcontrol"></div>
   </body>
{include file="footer.tpl"}