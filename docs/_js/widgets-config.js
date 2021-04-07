/* ==========================================================================
   Widgets Config
   ========================================================================== */

var widgetsConfig = function() {

	// Hide header on scroll
	navbar = $('.navbar');
	navbar.hideNavbarOnScroll({
		'deltaBeforeHide' : 5,
		'hideSpeed'       : 0.4,
	});

	// Skrollr init
	if (matchMedia('(min-width: 1140px)').matches) { 
		skrollr.init( { 
			forceHeight: false 
		});
	}

	// Tooltips
	var $allTooltips = $('[rel=tooltip]');
	$allTooltips.tooltip({placement: 'top'}).css('z-index', 2080);	

}

var repositionWelcome = function() {

	// Welcome text for hero
	$welcome 	= $('.welcome');
	welcomeTop  = ((windowHeight/2) - ($welcome.height()/2)) + "px";
	$welcome.css({ position : 'absolute', top : welcomeTop });

}

var repositionScrollDownArrow = function() {

	// Scroll down arrow for hero
	$scrollDownArrow 	= $('.hero-scroll-down-arrow');
	scrollDownArrowTop  = windowHeight - ($scrollDownArrow.height()*2) + "px";
	$scrollDownArrow.css({ position : 'relative', top : scrollDownArrowTop });
}

