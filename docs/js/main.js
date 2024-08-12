/* ==========================================================================
   Avoid `console` errors in browsers that lack a console.
   ========================================================================== */

(function() {
    var method;
    var noop = function () {};
    var methods = [
        'assert', 'clear', 'count', 'debug', 'dir', 'dirxml', 'error',
        'exception', 'group', 'groupCollapsed', 'groupEnd', 'info', 'log',
        'markTimeline', 'profile', 'profileEnd', 'table', 'time', 'timeEnd',
        'timeStamp', 'trace', 'warn'
    ];
    var length = methods.length;
    var console = (window.console = window.console || {});

    while (length--) {
        method = methods[length];

        // Only stub undefined methods.
        if (!console[method]) {
            console[method] = noop;
        }
    }
}());

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

