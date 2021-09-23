/*
*
*  jQuery Hide Navbar on Scroll plugin for jQuery 1.11.1 and Bootstrap 3.2.0
*  v1.0
*  ---
*  Copyright 2014, Antonio Gómez-Maldonado (http://antoniogomez.me)
*  Released under the MIT, BSD, and GPL Licenses.
*
*/

(function($) {

	$.fn.hideNavbarOnScroll = function(options) {

		// Plugin defaults
		var defaults = {
			deltaBeforeHide: 5,
			hideSpeed: 0.2,
			isActive : true
		}

		// Main variables
		var $window = $(window);

		var $document, $body;

		var plugin 	  		= {};
			plugin.settings = {};
			plugin.el 		= this.selector;
			plugin.$el 		= $(this.selector);

		var didUserScroll, navbarHeight, lastScrollTop;

		// Plugin init
		var init = function() {

			plugin.settings = $.extend({}, defaults, options);

			$document = $(document);
			$body 	= $('body');

			setNavbar();

		}

		// Setting the header function
		var setNavbar = function () {

			// Setting the header height
			navbarHeight = plugin.$el.outerHeight();

			// Creating a new css classes on the fly and appending them to the head of the page
			$('<style>').prop('type', 'text/css').html('\.header-up {\ top: -' + navbarHeight + 'px;\ } ' + plugin.el + ' {\ transition: top ' + plugin.settings.hideSpeed + 's ease-in-out; \ }').appendTo('head');

			// Adding the class to the header
			plugin.$el.addClass('header-down');
		}

		// Checking if the window has scrollbar
		var windowHasScrollBar = function() {

			return $body.height() > $window.height();

		}

		// User has scrolled
		var userHasScrolled = function () {

			var currentScrollTop = $(this).scrollTop();
			var $navbarCollapse 	 = plugin.$el.find('.navbar-collapse');

			// User scrolled less than the delta
		    if(Math.abs(lastScrollTop - currentScrollTop) <= plugin.settings.deltaBeforeHide)
		        return;

			// User scrolled down and past the header, add the class .header-up.
			if (currentScrollTop > lastScrollTop && currentScrollTop > navbarHeight) {

				// Closing the collapse responsive menu
				if (matchMedia('(max-width: 768px)').matches) {

					// Checking if the navbar is open
					if($navbarCollapse.hasClass('in')) {

						$navbarCollapse.collapse('hide');

					}

		    	}

		    	// Closing all dropdowns from the menu
		    	plugin.$el.find('[data-toggle="dropdown"]').parent().removeClass('open');

		    	// Adding the 'header-up' class to the header to hide it
				plugin.$el.removeClass('header-down').addClass('header-up');

			} else {

				// Adding the 'header-down' class to the header to show it
				if(currentScrollTop + $window.height() < $document.height()) {

					plugin.$el.removeClass('header-up').addClass('header-down');

				}

			}

			// Setting the new scroll top value
			lastScrollTop = currentScrollTop;

		}		

		// Window scroll event
		$window.scroll(function(event){

    		didUserScroll = true;

		});

		// Checking if the window has scrollbar and the user has scrolled
		setInterval(function() {

			if( windowHasScrollBar() ) {

				if ( didUserScroll && plugin.settings.isActive) {

					userHasScrolled();
					didUserScroll = false;

				}

			} else {

				plugin.$el.removeClass('header-up').addClass('header-down');

			}

		}, 250);

		// Starting the plugin
		init();
	}

})(jQuery);