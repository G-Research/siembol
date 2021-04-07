/* ==========================================================================
   Google Maps API Configuration.
   ========================================================================== */

var latLng;
var domMap;
var marker;

function initialize() {

    latLng = new google.maps.LatLng(contactLatitude, contactLongitude);

    var mapOptions = {
        zoom: 15,
        center: latLng,
        mapTypeId: google.maps.MapTypeId.ROADMAP,
        scrollwheel: false,
        draggable: false
    };

    domMap = new google.maps.Map(document.getElementById('map-canvas'), mapOptions);

    var infoWindowTemplate = '<div style="padding: 30px 30px;"><p style="line-height: 20px;"><strong>' +  markerTitle + '</strong></p><p>' + markerAddress + '</p></div>';

    var infowindow = new google.maps.InfoWindow({
        content: infoWindowTemplate
    });

    marker = new google.maps.Marker({
        position: latLng,
        map: domMap,
        title: 'Dropped Marker'
    });

    infowindow.open(domMap, marker);

    google.maps.event.addListener(marker, 'click', function() {
        infowindow.open(domMap, marker);
    });
}

google.maps.event.addDomListener(window, 'load', initialize);