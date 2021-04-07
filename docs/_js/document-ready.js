/* ==========================================================================
   Document Ready
   ========================================================================== */

$(document).ready(function() {

	// Main variables
	windowHeight 		      = $(window).height();
    windowWidth 		      = $(window).width();

    // Video hero height
	$videoHero = $('#video-hero');
   	$videoHero.height(windowHeight);

    // Image hero height
	$imageHero = $('#image-hero');
   	$imageHero.height(windowHeight);

	// Welcome text for hero section
	repositionWelcome();

	// Scroll down arrow for hero
	repositionScrollDownArrow();

	// Widgets config
	widgetsConfig();

});
