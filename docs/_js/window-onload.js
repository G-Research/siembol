/* ==========================================================================
   Window On Load
   ========================================================================== */

$(window).load(function() {

	// Main variables
	var windowHeight, windowWidth, welcomeTop;
	var $videoHero, $imageHero, $welcome, $scrollDownArrow;

	// Mute video background
	var $videoBackground 	  = document.getElementById('video_background');
	if($videoBackground != undefined) {
		$videoBackground.muted	= "muted";
	}

})
