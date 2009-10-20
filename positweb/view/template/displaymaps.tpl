{include file="header.tpl" title="Maps" tab="maps"}
<script>
   {literal}
   google.load("maps", "2.x");
   // Call this function when the page has been loaded
   function initialize() {
     var map = new google.maps.Map2(document.getElementById("map"));
     map.setCenter(new google.maps.LatLng(37.4419, -122.1419), 13);
   }
   google.setOnLoadCallback(initialize);
   {/literal}
   </script>
   <body>
   <div id="maps" style="width: 500px; height: 600px"></div>
    <div id="searchcontrol"></div>
   </body>