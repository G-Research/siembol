/* ==========================================================================
   Window Resize
   ========================================================================== */

$(window).resize(function() {

	// Window size variables update
	windowHeight	= $(window).height();
	windowWidth 	= $(window).width();

	// Video and Image hero height update
	$videoHero.height(windowHeight);
	$imageHero.height(windowHeight);

	// Welcome text for hero section
	repositionWelcome();

	// Scroll down arrow for hero
	repositionScrollDownArrow();

})