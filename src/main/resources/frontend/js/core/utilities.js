/*
	UTILITIES.js
	For small things.
*/
var debug = false;

/*	scratch
	put some text in our scratch pad
*/
function scratch(str) {
	if (debug) {
		$("#scratchpad").text(str);
	} else {
		console.log(str);
	}
}


/*	scratch json
	put some nicely formatted json in our scratch pad
*/
function scratch_json(json) {
	if (debug) {
		scratch(JSON.stringify(json, null, 2));
	} else {
		console.log(json);
	}
}


/*	better set interval
	a better wrapper for setInterval

	Arguments:
		waitTime: time between function calls
		fn:       the function to be called
		args:     arguments to be applied to fn when called
*/
function betterSetInterval(waitTime, fn, args) {
	var intervalId = setInterval(
		function() {
			if (Array.isArray(args)) {
				fn.apply(this, args);
			} else {
				fn(args);
			}
		},
		waitTime
	);

	return intervalId;
}


/*	scrollbar width
	return scrollbar's width
*/
$.scrollbarWidth = function() {
	var parent, child, width;

	if (width === undefined) {
		parent = $("<div/>").css({
			width: 50,
			height: 50,
			overflow: 'auto'
		}).append("<div/>");

		parent.appendTo('body');

		child = parent.children();
		width = child.innerWidth()-child.height(99).innerWidth();
		parent.remove();
	}

	return width;
};


/*	highlight element
	Highlight an element for a set amount of time.
*/
function highlightElement(element, time, highlightColor) {
	var originalColor = $(element).css("background-color");

	$(element).animate({"background-color": highlightColor}, {
		duration: 100,
		easing: "linear",
		done: function() {
			setTimeout(function() {
				$(element).animate({"background-color": originalColor}, {duration: 300});
			}, time);
		}
	});
}


/*	fade remove
	Fade an item out and remove it from the DOM after it finishes fading
	out.
*/
function fadeRemove(elementID) {
	var element = $("#" + elementID);
	element.stop();
	element.animate({"opacity": "0"}, {
		duration: 100,
		easing: "linear",
		done: function() {
			this.remove();
		}
	});
}


/*	fade remove, as a jQuery function
	Does the same as the above fadeRemove function, but you can call it on
	jQuery objects...
*/
$.fn.fadeRemove = function() {
	this.stop().animate(
		{"opacity": "0"},
		{ duration: 100,
			easing: "linear",
			done: function() {
				this.remove();
			}
		}
	);
};


/*	date formate
	format a date (called `date`, oh my god!)
	return a string form of the date in MM/DD/YYYY hh:mm format
*/
function dateFormat(date) {
	// if this isn't a date, return nothing.
	// obviously it's a user error...
	if (isNaN(date.getDate())) {
		return "";
	}

	dstring = "";
	dstring += ((date.getMonth() + 1) < 10 ? "0" : "") + (date.getMonth() + 1);
	dstring += "/";
	dstring += (date.getDate() < 10 ? "0" : "") + date.getDate();
	dstring += "/";
	dstring += date.getFullYear();
	dstring += " ";

	//get the hour number
	var hour = date.getHours();

	//get the am or pm value
	var amOrPm = hour < 12 ? "AM" : "PM";

	//if it is the midnight hour
	if (hour === 0) {
		//show 12 instead of 0
		hour = 12;
	}
	//if it is afternoon
	else if (hour > 12) {
		//get the pm hour
		hour = hour - 12;
	}

	dstring += (hour < 10 ? "0" : "") + hour;
	dstring += ":";
	dstring += (date.getMinutes() < 10 ? "0" : "") + date.getMinutes();
	dstring += ":";
	dstring += (date.getSeconds() < 10 ? "0" : "") + date.getSeconds();
	dstring += " " + amOrPm;

	return dstring;
}


function getSelectedElements() {
	var selection = window.getSelection();

	// return an array of nulls
	if (selection.type != "Range") {
		return [null, null];
	}

	var elements = $(selection.getRangeAt(0).cloneContents()).children();

	// We had to use a regular for loop and not a for each because the for each
	// was an asynchronous call.
	return _.map(elements, function(element) { return element.id; });
}

/*	highlight elements
	Highlights a group of elements from start id to and end id
*/
function highlightElements(start, end) {
	var currentID = start;
	while (currentID != end) {
		$("#" + currentID).addClass("highlight");
		currentID = $("#" + currentID).next().attr("id");
	}

	$("#" + currentID).addClass("highlight");
}

/*	show good notification
	Shows a notification in good colors based off of the `html`.
	It will hide itself after `timeToShow` seconds
*/
function showGoodNotification(html, timeToShow) {
	showNotification(html, timeToShow, "goodNotification");
}

/*	show bad notification
	Shows a notification in bad colors based off of the `html`.
	It will hide itself after `timeToShow` seconds
*/
function showBadNotification(html, timeToShow) {
	showNotification(html, timeToShow, "badNotification");
}

/*	show neutral notification
	Shows a notification in neutral colors based off of the `html`.
	It will hide itself after `timeToShow` seconds
*/
function showNeutralNotification(html, timeToShow) {
	showNotification(html, timeToShow, "neutralNotification");
}

/*	show notification
	Shows a notification based on the passed in `html`.
	It will hide itself after `timeToShow` miliseconds.
	It's `className` will determine it's style (good/bad/neutral)
*/
function showNotification(html, timeToShow, className) {
	// Remove the old notification shown to make room for the new one.
	hideNotification();

	// Create the element and add it to the screen.
	var notification = $("<div/>", {
		class: className,
	});
	notification.html(html);

	// Add an exit button to the end of the notification.
	var closeButton = $("<button/>", {
		click: hideNotification,
		class: "removeNotification",
	});
	closeButton.html("<image src='/img/xblack.svg'></image>");
	closeButton.appendTo(notification);

	$("#notification").hide().append(notification).slideDown();

	// Create a timeout so we can delete the element after some time.
	$("#notification").data("timeToShow", setTimeout(hideNotification, timeToShow));
}

/*	hideNotification
	Hides the notification bar with a nice animation.
*/
function hideNotification() {
	// Clear the timer so it doesn't try to hide it again later.
	clearTimeout($("#notification").data("timeToShow"));

	// Animate it sliding up, than empty the notifications div.
	$("#notification").find("div").slideUp(function() {
		$("#notification").empty();
	});
}
