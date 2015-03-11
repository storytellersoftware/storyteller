/*
	COMMENTS.js
	All the stuff related to clip comments

	Requirements:
		playback.js - because we need to be able to pause when a comment is shown
		login.js - because we need the developerGroups object
		utilities.js - because we need to fade/remove the comment items
*/

// this will hold all of our comments keyed by their eventID
var comments = {};

/*	add comments
	This creates the comment hash map from a list of comments.
*/
function addComments(clipNum, commentsList) {
	$.each(commentsList, function(i, comment) {
		comments[clipNum + "-" + comment.eventID] = comment;
	});
}

/*	check event for comments
	Checks if an event has a comment, if it does, it is added
	to the orderEvent object and orderEvent is returned.
*/
function checkEventForComments(orderEvent) {
	var commentID = orderEvent.clipNumber + "-" + orderEvent.eventID;

	if (commentID in comments) {
		orderEvent.commentID = commentID;
	}

	return orderEvent;
}

/*	show comment
	Adds a comment with the id of `commentID` to the comment section.
	It has a special id in the DOM starting with "comment" so it does
	not share an id with an event.
	This will also pause the playback.
*/
function showComment(commentID) {
	addToCommentSection("comment-" + commentID, "comment", comments[commentID].commentText,
		developerGroups[comments[commentID].developerGroupID].developers);

	if (playback.playing) {
		playPause();
	}

	highlightElements(
		playback.clipNumber + "-" + comments[commentID].startHighlightedEventID,
		playback.clipNumber + "-" + comments[commentID].endHighlightedEventID
	);
}

/*	hide comment
	Fades/Removes a comment/clip/discription from the DOM.
*/
function hideComment(commentID) {
	fadeRemove(commentID);
}

/*	show comment section
	Shows the comment section
*/
function showCommentSection() {
	$("#documents").css("width", "70%");
	$("#comments").show();
}

/*	hide comment section
	Hides the comment section
*/
function hideCommentSection() {
	$("#documents").css("width", "100%");
	$("#comments").hide();
}

/*	show clip info
	Adds clip info, from a clip in the list `storyboard.clips`,
	into the comment section
*/
function showClipInfo(clipNumber) {
	var html = "<h2>" + storyboard.clips[clipNumber].name + "</h2>";
	html += "<p>" + storyboard.clips[clipNumber].description + "</p>";

	addToCommentSection(
		"clip-comment-" + clipNumber, "clip", html,
		developerGroups[storyboard.clips[clipNumber].developerGroupID].developers
	);
}

/*	hide clip info
	Fades/Removes the specified clip
*/
function hideClipInfo(clipNumber) {
	fadeRemove("clip-comment-" + clipNumber);
}

/*	show description
	Adds the description of a storyboard (it's title and description)
	to the comments section
*/
function showDescription(storyboard) {
	var html = "<h1>" + storyboard.name + "</h1>" + "<p>" + storyboard.description + "</p>";

	addToCommentSection(
		"descriptionComment", "description", html,
		developerGroups[storyboard.developerGroupID].developers
	);
}

/*	add to comment section
	Creates an element to add to the comment section with the
	specified id, and class name. The html passed in because
	comments don't have a name but storyboards and clips do.

	This will put the developer images and other passed in html
	in the section by fading/highlighting it in.
*/
function addToCommentSection(id, className, html, developerIDs) {
	// Create the images
	var imgs = $("<div/>", {
		class: "commentImgs"
	});

	// For each developer, add the image to the imgs div
	$.each(developerIDs, function(i, devID) {
		var dev = developers[devID];

		$("<img/>", {
			src: dev.gravatar + "&s=50"
		}).appendTo(imgs);
	});

	// Create the new comment pre and add imgs and html to it
	// Then place it in the comments section
	var newComment = $("<pre/>", {
		id: id,
		class: className,
		style: "opacity: 0;", // The opacity is faded in later
	});

	newComment.append(imgs);
	newComment.append(html);
	newComment.appendTo("#comments");

	if(className == "comment") {
		newComment.click(function(event) {
			commentClick(id, $(this).attr("id"));
		});
	} else if(className == "clip") {
		newComment.click(function(event) {
			clipClick(id, $(this).attr("id"));
		});
	} else {
		newComment.click(function(event) {
			storyboardClick();
		});
	}

	$("#comments").show();

	// Scroll to the new comment
	$("#comments").scrollTo(newComment);

	// Start animating
	// Save the original color to revert to later
	var originalColor = newComment.css("background-color");

	// Start animating the background-color and the opacity
	newComment.animate(
		{
			"background-color": "#cade97",
			"opacity": "1",
		},
		{
			duration: 100,
			easing: "linear",

			// When the animation is done, it needs to wait for some time, then revert
			// back to it's original color
			done: function() {
				setTimeout(function() {
					newComment.animate({"background-color": originalColor}, {duration: 300});
				}, 500);
			}
		}
	);
}
/*
	GLLOBAL.js

	Generic Storyteller chrome stuff. As in, not modular, and needs
	to be on every page. EVERY SINGLE PAGE!
*/

// the, you know, playback session's id?
// This is here because it's used everywehere, and should probably
// only be declared once.
var playbackSessionID;

function setPlaybackSessionId(newPlaybackSessionId) {
	//set the playback session id
	playbackSessionID = newPlaybackSessionId;

	//adjust the links so that each page knows the current playback session id
	$("#playback-link").attr('href', "playback.html?sessionID=" + newPlaybackSessionId);
	$("#create-storyboard-link").attr('href', "create-storyboard.html?sessionID=" + newPlaybackSessionId);
	$("#storyboard-link").attr('href', "storyboard.html?sessionID=" + newPlaybackSessionId);
}

function getPlaybackSessionId() {
	return playbackSessionID;
}

/*	get search data
	Pick apart the query string in the page's address and return the
	key/value pairs.
*/
function getSearchData() {
	//object with name value pairs in the url string
	var searchParams = {};

	//if there is a string to parse
	if (window.location.search !== '' && window.location.search.length > 1) {
		//throw out the ?
		var searchString = window.location.search.substring(1);

		//split by & for params
		var pairs = searchString.split('&');

		//for each name/value pair from the url
		$.each(pairs, function(i, pair) {
			//split into key/value
			var pairSplit = pair.split('=');

			//add to searchParams
			searchParams[pairSplit[0]] = pairSplit[1];
		});
	}

	return searchParams;
}

/*	set mode
	Changes the 'mode' text in the header (it's 'playback' by default),
	to show that something new is happening.

	If you go into clip creation mode, it changes the header's class, so
	that we can change the styles (if we wanted to). Other modes can also
	have this feature.
*/
function setMode(str) {
	$("#mode").text(str);

	if (str == 'clip creation') {
		$("header").attr('class', 'clipcreation');
	} else {
		$("header").attr('class', '');
	}
}

/* Long name stuff... We aren't worrying about this right now.
$(window).resize(function() {
	$("header h1").width($("header").innerWidth() - $("header .nav").outerWidth() - 50)
})
$(document).ready(function() {
	$("header h1").width($("header").innerWidth() - $("header .nav").outerWidth() - 50)
})
*/
/*
	KEYBOARD-SHORTCUTS.js
	Contains all of our keyboard shortcuts
*/

function setupKeyboardShortcuts() {

	// Key Codes:
	var keys = {
		tab:         9,
		enter:      13,
		space:      32,

		arrowLeft:  37,
		arrowRight: 39,
		arrowUp:    38,
		arrowDown:  40,

		comma:     188,   // shift <
		period:    190,   // shift >
	};


	/*
	$(document).keydown(function(event) {
		// show which character is pressed
		console.log(event.which);
	})
	*/


	/*	Playback Shortcuts

		Spacebar - play or pause
		Right arrow/> - go farward in playback
		Left arrow/< - go backwards in playback
		Up/down arrow - change speed
	*/
	$("#playbackArea").keydown(function(event) {
		// Make sure the focus is not on an input element
		if (!$("*:focus").is("textarea, input")) {
			// if we hit the space bar, play/pause
			if (event.which === keys.space) {
				playPause();
			}

			// right arrow  and `>` (well, `.`)
			if (event.which === keys.period || event.which === keys.arrowRight) {
				if (playback.playing) {
					playPause();
				}

				stepForward(1);
			}

			// left arrow and `<` (well, `,`)
			if (event.which === keys.comma || event.which === keys.arrowLeft) {
				stepBackward(1);
			}

			// up arrow
			if (event.which === keys.arrowUp) {

				// Increment the speed
				$("#speedSlider").slider('option',
					'value', parseInt($("#speedSlider").slider('value')) + 1);

				// Change the speed
				changeSpeed($("#speedSlider").slider('value'));

				// Clear an old timeout to hide the slider
				clearTimeout($("#speedHolder").data("leaveTimeout"));

				// Set a new timout
				$("#speedHolder").data(
					"leaveTimeout", setTimeout(hideSpeedSlider, 1500));

				// Show the slider
				if (!$("#speedHolder").is(":visible")) {
					toggleSpeedSlider();
				}
			}

			// down arrow
			if (event.which === keys.arrowDown) {
				// Decrement the speed
				$("#speedSlider").slider(
					'option', 'value', parseInt($("#speedSlider").slider('value')) - 1);

				// Change the speed
				changeSpeed($("#speedSlider").slider('value'));

				// Clear an old timeout to hide the slider
				clearTimeout($("#speedHolder").data("leaveTimeout"));

				// Set a new timout
				$("#speedHolder").data(
					"leaveTimeout", setTimeout(hideSpeedSlider, 1500));

				// Show the slider
				if (!$("#speedHolder").is(":visible")) {
					toggleSpeedSlider();
				}
			}
		}
	});


	// filterSelect Menu
	// Enter - submit the filters
	$("#filterSelect").keydown(function(event) {
		if (event.which === keys.enter) {
			$("#filterSelect").dialog("close");
			setupPlaybackInterface();
			submitFilters();
		}
	});

/*
	// login Menu
	// enter - cause login to occur
	$("#loginbox").keydown(function(event) {
		if (event.which == keys.enter) {
			doLogin();
			}
	});
*/

	// storyboard title
	// tab - open up description box, and switch to it
	$("#storyboardTitle").keydown(function(event) {
		if (event.which === keys.tab && !$("#description").is(':visible')) {
			event.preventDefault();
			toggleDescription();

			$("#storyboardDescription").focus();
		}
	});

	// storyboard description
	// tab - close description box, and switch to title
	$("#storyboardDescription").keydown(function(event) {
		if(event.which == keys.tab) {
			event.preventDefault();
			toggleDescription();
			$("#storyboardTitle").focus();
		}
	});
}
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
	
	/*  Show milliseconds - we don't want that, but if we do, it's here
			for future reference

	dstring += ":";
	dstring += (date.getMilliseconds() < 100 ? 
			date.getMilliseconds() < 10 ? 
					"00" : 
					"0" : 
			"");
	dstring += date.getMilliseconds();
	*/

	return dstring;
}



function getSelectedElements() {
	var ids = [];

	var selection = window.getSelection();

	// return an array of nulls
	if(selection.type != "Range"){
		return [null,null];
	}

	var elements = $(selection.getRangeAt(0).cloneContents()).children();

	// We had to use a regular for loop and not a for each because the for each
	// was an asynchronous call.
	for (var i = 0; i < elements.length; i++) {
		ids.push(elements[i].id);
	}

	return ids;
}

/*	highlight elements
	Highlights a group of elements from start id to and end id
*/
function highlightElements(start, end) {
	var currentID = start;
	while(currentID != end) {
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
/*
	playback/MAIN.js

	Contains our fun stuff for viewing a PLAYBACK!

	Requirements:
		- login.js
			Contains everything needed for specifying what developer you are.
			Also holds developers and developerGroups objects, which are used
			in the filter menu and in showing what developer groups wrote what
			code.
	
		- filtering.js
			Contains everything needed to filter events received from the server
			based on a filters object (also in filtering.js).

		- clip-creation.js
			Contains everything needed for creating clips.

		- global.js
			Because it's needed everywhere?

		- keyboard-shortcuts.js
			All of our keyboard shortcuts are kept there.

		- rightclick.js
			The poor abstraction of right clicking stuff is stored there.
			// TODO - change up how right click stuff works


		- playback/filter-menu.js
			Contains the filter menu stuff, and only the filter menu stuff.
			Just the things for letting a user choose what they want to see.

		- playback/movement.js
			Contains the movement stuff for playbacks, as in making things appear
			and disappear from the screen.

		- playback/settings.js
			Contains the settings menu stuff for playbacks.

		- playback/speed.js
			Contains the stuff for adjusting the speed of a playback.
*/

// object of ALL events, keyed to an event's ID
var events = {};

// holds metadata about the playback
var playback = {
	speed: 100,         // speed of playback in ms
	fontSize: 14,       // font size for playback documents
	orderOfEvents: [],  // all events, in order of occurrence 

	//TODO when we have time to do it right, change the name of this to relevantEventIndexValues 
	relevantEvents: [], // position of relevant events in orderOfEvents (used for playback slider) in order of occurrence
	eventsWithCommentsIndexValues: [], //holds the index in orderOfEvents where there are comments
	playing: false,     // if we"re currently paused or playing (paused = false)
	position: 0,        // the current position in the playback
	player: null,       // the interval for our playing
	type: 'filtered',   // the type of playback - filtered, selection, clip, or storyboard
	clipNumber: 0,      // the current clip we are playing
	documentID: "",     //id of the document that has the focus
	animate: true,      //whether or not we should animate the playback
};


function setupPlayback() {
	//retrieve settings from localstorage
	getSavedSettings();

	//set the font size of the playback documents
	changeFontSize(playback.fontSize);

	//setup the back seek button to let you hold it,
	//causing stepBackward() to be called a bunch.
	$("#stepBack").mousedown(function() {
		if (playback.playing) playPause();

		clearInterval($(this).data("timeout"));
		clearInterval($("#stepForward").data("timeout"));

		//causes an initial step back if just one click
		stepBackward(1);

		//only called if held down (because of the timeout),
		//attaches an interval id to the button, which calls stepBackward
		//every `playback.speed` ms
		$(this).data("timeout", setInterval(function() { stepBackward(1); }, playback.speed));
	}).bind("mouseup mouseleave", function() {
		//stops the above interval
		clearInterval($(this).data("timeout"));
	});

	//same as the back seek button, but for the forward seek button
	$("#stepForward").mousedown(function() {
		if (playback.playing) playPause();

		clearInterval($(this).data("timeout"));
		clearInterval($("#stepBackward").data("timeout"));

		//causes initial step if just one click
		stepForward(1);

		//only called if held down, attaches an interval id to the button,
		//which calls stepForward every `playback.speed` ms
		$(this).data("timeout", setInterval(function() { stepForward(1); }, playback.speed));
	}).bind("mouseup mouseleave", function() {
		//stops the above interval
		clearInterval($(this).data("timeout"));
	});


	//setup the tab area for playback documents
	$("#documents").tabs({
		collapsible:  false,  //can we collapse the tabs? NO
		hide:         false,  //is this hidden? NO
		active:       1,      //which tab are we starting with? 1
		activate:     function(event, ui) {
			// Called when tabs are clicked
			if(playback.playing && playback.documentID !== '') {
				pause();
			}
			playback.documentID = ui.newPanel[0].id;                  
		}
	});

	// get any data from the url
	// ex: url: ../playback.html?parameter1=1&parameter2=2
	//      => {parameter1: 1, parameter2: 2}
	var searchData = getSearchData();

	//if there's a storyboardID param, we're playing back a storyboard
	if ('storyboardID' in searchData) {
		//set the playback type
		playback.type = 'storyboard';
		
		//store the id of the storyboard to playback
		storyboard.storyboardID = searchData.storyboardID;

		//if there is a session id in the url    
		if ('sessionID' in searchData) {
			setPlaybackSessionId(searchData.sessionID);
		}

		//TODO is this right?? change
		startGettingEvents();    
	}
	// if there's a clip ID, we're doing a clip.
	else if ('clipID' in searchData) {
		//set the playback type
		playback.type = 'clip';
		
		//store the id of the clip to playback
		clip.clipID = searchData.clipID;
		
		//if there is a session id in the url    
		if('sessionID' in searchData) {
			setPlaybackSessionId(searchData.sessionID);
		}

		//TODO is this right?? change
		startGettingEvents();  
	}
	// if there's a selected text session ID, we're doing a selection playback
	else if ('selectedTextSessionID' in searchData) {
		//set the playback type
		playback.type = 'selection';
		
		//a playback session has already been created  
		//get the newly created selected text session id from the url and store 
		//it in the global session id 
		setPlaybackSessionId(searchData.selectedTextSessionID);
		
		//display that we're doing a selected text playback
		setMode("selected text playback");
		
		//selected text playbacks needs some filter info from the playback session to 
		//make devs show up, get the filter info from the session 
		getSelectedTextFilterInfo(getPlaybackSessionId());
		
		//the playback session is ready, start grabbing events
		grabAllEvents();    
	}
	//else this is a plain old playback with a user specified filter
	else if ('sessionID' in searchData){
		//set the playback type
		playback.type = 'filtered';
		
		//store the session id
		setPlaybackSessionId(searchData.sessionID);
		
		//set up the filter menu and start getting events     
		startGettingEvents();    
	}
	//bad request!
	else {
		//do something smart here
		window.alert("Improper playback request");
	}
	
	$("#playbackArea").focus();

	//setup the interface heights/widths and the right-click menu
	setupPlaybackInterface();
	setupPlaybackRightclickMenu();


	//create a clear event, with the ID of CLEAR
	events.CLEAR = {
		ID:               "CLEAR",
		type:             "CLEAR",
		developerGroupID: null,
	};
}

function getSelectedTextFilterInfo(sID) {
	//ask for a selected text playback session's filter info
	$.getJSON("/playback/filter/sessionID/" + sID,
		function(data) {
			//store the filter info from the server
			filterMenu = data.filters;

			//process the filter info (this is needed so the playback can refer to
			//developers and developer groups during the playback)
			setupFilters(data.filters);
			
			//set up the login window
			//setupLogin();      
		});
}

//when we resize the window, adjust the sizes of our elements
$(window).resize(function() {
	setupPlaybackInterface();
});


/*  setup playback interface
		Adjusts the heights and widths of several elements to fill the 
		entire screen.
*/
function setupPlaybackInterface() {
	// make the main playback area the whole height
	$("#playbackArea").height($(window).height() - 10 - $("header").outerHeight());
	
	// set document heights
	var h = $(window).height();
	h -= $("#commands").height();
	h -= $.scrollbarWidth();
	h -= $("header").outerHeight();

	var style = "#comments { height: " + h + "px; }";

	h -= $("#documentTabs").outerHeight();
	style += "\npre.document { height: " + h + "px; }";

	$("#interfaceStyles").html(style);

	//set location slider"s width
	var lhw = $(window).width() - $("#movement").outerWidth() - $("#etcCommands").outerWidth() - 70;

	$("#locationHolder").width(lhw);
	$("#timestamp").width(lhw);

	setupSpeedSlider();
}


/*  play/pause
		Plays or pauses the playback, depending on if we're currently
		playing or now. Also, change the icon the the corresponding icon.
*/
function playPause() {
	//clear the timers that are pushing the playback with the forward or 
	//backward with the buttons/arrow keys
	clearInterval($("#stepForward").data("timeout"));
	clearInterval($("#stepBackward").data("timeout"));

	//if we are already playing
	if (playback.playing) {
		//pause the playback
		pause();
	}
	//we are paused
	else {
		//start the playback
		play();
	}
}

/*Play
	Plays the playback
*/
function play() {
	//stop the timers pushing playback
	clearInterval($("#stepForward").data("timeout"));
	clearInterval($("#stepBackward").data("timeout"));
	
	//if we are paused
	if (!playback.playing) {
		//indicate we are playing 
		playback.playing = true;
		
		//set the time to automatically play at speed slider speed
		playback.player = setInterval(function() { stepForward(1); }, playback.speed);
 
		//toggle the buttons
		$("#pauseIcon").show();
		$("#playIcon").hide();
	}
}

/* Pause
	Simply Pause the playback
*/
function pause() {  
	//stop the timers pushing playback
	clearInterval($("#stepForward").data("timeout"));
	clearInterval($("#stepBackward").data("timeout"));
	
	//if we are playing 
	if (playback.playing) {
		//indicate we are not playing anymore (paused)
		playback.playing = false;
		//stop the timer to auto play
		clearInterval(playback.player);

		//toggle the buttons
		$("#pauseIcon").hide();
		$("#playIcon").show();
	}
}

/*  clear everything
		Clear out everything, in preparation for a new set of filters
*/
function clearEverything() {
	//clear out the main object of events
	events = {};
	
	//clear out the event grabber
	eventGrabber.done = false;
	eventGrabber.currentIndex = 0;
	
	//clear out the event ids and relevant event positions
	playback.orderOfEvents = [];
	playback.relevantEvents = [];

	//clear out the playback location slider
	$("#locationSlider").slider("option", "value", 0);
	$("#locationSlider").slider("option", "max", 0);

	//clear out the documents 
	$("#documents").html(
		"<ul id='documentTabs'></ul>\n" +
		"<div id='developerPictures'></div>"
	);

	//clear out the documents tabs
	$("#documentTabs").tabs({
		collapsible: false,
		hide: false,
		active: 1,
	});
}

/*  change dev group
		Change the currently displayed developer group
*/
function changeDevGroup(currentDevGroup) {
	//if the passed in developer group is NOT the same as the current dev group 
	if ($("#developerPictures").data("group") === undefined || 
		 $("#developerPictures").data("group") !== currentDevGroup ) {
		
		//search the dev groups for the passed in dev group
		//old
		//for(var i = 0; i < developerGroups.length; i++) {
		//new
		//go through each of the developer groups
		$.each(developerGroups, function(devGroupId, devGroup) {
			//if we have found the current dev group 
			if (currentDevGroup === devGroupId) {
			
				//store the dev group id
				$("#developerPictures").data("group", currentDevGroup);

				//clear out the div
				$("#developerPictures").html("");
				
				//for each of the developers in the dev group
				$.each(devGroup.developers, function(i, devID) {
					//create a picture of the dev
					$("<img/>", {
						class: "devPicture",
						src: developers[devID].gravatar,
						title: developers[devID].firstName + " " + developers[devID].lastName + " (" + developers[devID].email + ")",
					}).appendTo("#developerPictures");
				});     
			}
		});
	}
}

//MM- do we use this anymore????
/*  find event
		Finds the relative event number of a passed in eventID and it's clipNumber
*/
function findEvent(eventID, clipNumber) {
	//index of event in playback.orderOfEvents 
	var index = 0;

	//go through all the events until we come to the correct clip
	while(playback.orderOfEvents[index].clipNumber < clipNumber) {
		index++;
	}

	//now that we're in the right clip, find the event
	while(playback.orderOfEvents[index].eventID != eventID) {
		index++;
	}

	return index;
}


/*  find clip clear event
		Finds the relative event number of a clip clearing using the clipNumber passed in.
*/
function findClipClearEvent(clipNumber) {
	currentClipNumber = 0;
	for (var i = 0; i < playback.relevantEvents.length; i++) {
		if (events[playback.orderOfEvents[playback.relevantEvents[i]].eventID].type == "CLEAR") {
			currentClipNumber++;
		}

		if (currentClipNumber == clipNumber) {
			return playback.relevantEvents[i];
		}
	}
}
/*
	playback/MOVEMENT.js
	All things related to moving around in a playback. It's separate
	because there's too much for just playback.

	Requirements:
		- playback/main.js
			Because that's where all of the objects used are held, and all of
			the cool jquery ui stuff is setup.
*/


function setupMovement() {
	// setup the seek location slider down at the bottom
	$("#locationSlider").slider({
		animate: false,
		min: 0,
		max: 1,
		orientation: "horizontal",
		step: 1,
		range: "min",
		slide: function(event, ui) {
			// move to the specified location
			step(playback.relevantEvents[ui.value], false);
		}
	});
}


/*	step
	Move to a specified position (an index in playback.orderOfEvents).
*/
function step(position, animate) {
	// if we're playing, pause
	if (playback.playing) {
		playPause();
	}

	// determine if we are moving forward based on where we are and where we'd like to go
	var steppingForward = playback.position < position;

	// get the number of steps to get there
	var absSteps = Math.abs(playback.position - position);

	if (steppingForward) {
		//move in the forward direction
		stepForward(absSteps, animate);
	} else {
		//move in the backward direction
		stepBackward(absSteps, animate);
	}
}


/*	step forward
	Move forward a specified number of steps.
*/
function stepForward(steps, animate) {
	var i;

	//helpers...
	//MM- not sure if I like these
	function isRelevant() {
		//based on the playback position get the event from orderOfEvents and see if its relevant
		return playback.orderOfEvents[playback.position].relevant;
	}

	function currentEvent() {
		//based on the playback position get the event from orderOfEvents and get the full event from events
		return events[playback.orderOfEvents[playback.position].eventID];
	}

	function lastEvent() {
		//if the playback position is beyond the events in orderOfEvents then we're at the end
		return playback.position >= playback.orderOfEvents.length;
	}

	//clear out any highlight from a clip comment
	$(".highlight").removeClass("highlight");

	//if the user wants to skip animation
	if (playback.animate === false) {
		//if this is a filtered or selected text playback
		if (playback.type === 'filtered' || playback.type === 'selection') {
			//set the number of steps to a very large value to jump to the end
			steps = Number.MAX_VALUE;
		}
		//if this is a clip or storyboard playback
		else if (playback.type === 'clip' || playback.type === 'storyboard') {
			//go through all the index values where there is a comment
			for (i = 0; i < playback.eventsWithCommentsIndexValues.length; i++) {
				//if we find a comment index beyond the current position
				if (playback.eventsWithCommentsIndexValues[i] > playback.position) {
					//calculate how many steps it will take to get there
					steps = playback.eventsWithCommentsIndexValues[i] - playback.position + 1;
					break;
				}
			}
		}

		//indicate there should be no animation
		animate = false;
	}


	//default to animated
	if (animate === undefined) {
		animate = true;
	}

	//for each of the desired steps
	for (i = 0; i < steps; i++) {
		//step forward (if we've reached the end of the playback, return)
		if (!doStepForward(animate)) {
			break;
		}

		//if the current event isn't the last and it isn't relevant, move forward
		while (!lastEvent() && !isRelevant() && doStepForward(animate)) {
			//update the number of steps taken for irrelevant events
			i++;
		}

		//if this is the last event
		if (lastEvent()) {
			//move the slider to the end
			$("#locationSlider").slider('value', playback.relevantEvents.length - 1);
		}
		//not at the end of the events yet
		else {
			//move the slider to the position of the latest relevant event
			$("#locationSlider").slider('value', playback.relevantEvents.indexOf(playback.position));

			// Display the timestamp as long as the event is not undefined
			// (This only happens at the end)
			if (currentEvent() !== undefined ) {
				//display the time stamp
				$("#timestamp").text(dateFormat(new Date(currentEvent().timestamp)));

				//show the dev group that made this event
				changeDevGroup(currentEvent().developerGroupID);
			}
		}
	}
}


/*	do step forward
	This will take one step forward with the option to animate it or not.
	Generally things are animated if they are relevant and not animated
	if they are irrelevant.
*/
function doStepForward(animate) {
	//helpers...
	//MM- not sure if I like these
	function getCurrent() {
		//based on the playback position get the event from orderOfEvents and get the full event from events
		return events[playback.orderOfEvents[playback.position].eventID];
	}

	function getOrderCurrent() {
		//based on the playback position get the event from orderOfEvents
		return playback.orderOfEvents[playback.position];
	}

	//if we've reached the end of the playback
	if (playback.position >= playback.orderOfEvents.length) {
		//if we are playing, pause
		if (playback.playing) playPause();

		//indicate that we are done stepping forward
		return false;
	}

	//check the event type
	if (getCurrent().type == "CREATE-DOCUMENT") {
		//create a new document tab in the gui
		createDocument(getOrderCurrent(), animate);
	}
	else if (getCurrent().type == "INSERT") {
		//create an element in the dom for the new event
		insert(getOrderCurrent(), true, animate);
	}
	else if (getCurrent().type == "DELETE") {
		//highlight and remove an event from the dom
		del(getOrderCurrent(), true, animate);
	}
	else if (getCurrent().type == "CLEAR") { //end of a clip
		//display the next clip
		hideClip(getOrderCurrent());
		showClipInfo(getOrderCurrent().clipNumber + 1);

		//increase the clip number to move on to the next one
		playback.clipNumber++;
	}

	//if there is a commentID in the current event
	if ('commentID' in getOrderCurrent()) {
		//show the comment on the screen
		showComment(getOrderCurrent().commentID);
	}

	//move to the next event
	playback.position = playback.position + 1;

	//indicate that there are more events to handle
	return true;
}


/*	step backward
	Take the specified number of steps backwards in the playback
*/
function stepBackward(steps, animate) {
	//remove any highlight from a clip comment
	$(".highlight").removeClass("highlight");

	//helpers...
	//MM- not sure if I like these
	function isRelevant() {
		//based on the playback position get the event from orderOfEvents and see if its relevant
		return playback.orderOfEvents[playback.position].relevant;
	}

	function currentEvent() {
		//based on the playback position get the event from orderOfEvents and get the full event from events
		return events[playback.orderOfEvents[playback.position].eventID];
	}

	function firstEvent() {
		//if the playback position is at 0 (or below) we are at the beginning
		return playback.position <= 0;
	}

	//if we're currently playing, pause (we'll start playing again at the end).
	//This is for when you hold the step back button
	if (playback.playing) {
		playPause();
	}

	//if we're at the beginning there is no where else to go
	if (playback.position === 0) {
		return;
	}

	//default to animated
	if (animate === undefined) {
		animate = true;
	}

	//for all the desired steps
	for (var i = 0; i < steps; i++) {
		//if we've reached the beginning, stop moving
		if (!doStepBackward(animate)) {
			break;
		}

		//if our current event isn't relevant, keep moving backwards
		while (!firstEvent() && !isRelevant()) {
			if (!doStepBackward (animate)) {
				break;
			}

			//keep track of all the steps we are taking for irrelevant events
			i++;
		}

		//Move the slider
		$("#locationSlider").slider('value', playback.relevantEvents.indexOf(playback.position));

		//Display the timestamp as long as the event is not undefined
		//(This only happens at the end)
		if (currentEvent() !== undefined) {
			//display the time of the event
			$("#timestamp").text(dateFormat(new Date(currentEvent().timestamp)));

			//change the developer group picture (if necessary)
			changeDevGroup(currentEvent().developerGroupID);
		}
	}
}


/*	do step backward
	This will take one step backward with the option to animate it or not.
	Generally things are animated if they are relevant and not animated
	if they are irrelevant.
*/
function doStepBackward(animate) {
	//helpers...
	//MM- not sure if I like these
	function getCurrent() {
		//based on the playback position (in reverse direction) get the event from
		//orderOfEvents and pull out the full event
		return events[playback.orderOfEvents[playback.position - 1].eventID];
	}

	function getOrderCurrent() {
		//get the event from orderOfEvents (in reverse direction)
		return playback.orderOfEvents[playback.position - 1];
	}

	//if we are at the beginning
	if (playback.position === 0) {
		//indicate there is no where else to go
		return false;
	}

	//in reverse, a create document should be treated as a delete document
	if (getCurrent().type == "CREATE-DOCUMENT") {
		delDocument(getOrderCurrent());
	}
	//in reverse, an insert should be treated as a delete
	else if (getCurrent().type == "INSERT") {
		del(getOrderCurrent(), false, animate);
	}
	//in reverse, a delete should be treated as an insert
	else if (getCurrent().type == "DELETE") {
		insert(getOrderCurrent(), false, animate);
	}
	//in reverse, a clip end should cause us to go to the previous clip
	else if (getCurrent().type == "CLEAR") {
		//show next clip, hide old clip
		showClip(getOrderCurrent());
		hideClipInfo(getOrderCurrent().clipNumber +1);

		//move to the previous clip
		playback.clipNumber--;
	}

	//if there is a commentID member in the current event
	if ('commentID' in getOrderCurrent()) {
		//in reverse, we hide the comment
		hideComment("comment-" + getOrderCurrent().commentID);
	}

	//move to the previous event
	playback.position = playback.position - 1;

	//indicate that there are more events to process
	return true;
}



/*	insert
	When we have an insert event (going forward), or a
	delete event (going backward), we add the event to the
	screen.
*/
function insert(orderEvent, forwards, animate) {
	//get the full event from the passed in event
	var event = events[orderEvent.eventID];

	//is it relevant?
	var relevant = orderEvent.relevant;

	//should it be displayed as being relevant even if its not (perfect programmer, non-animated case)
	var displayRelevantButDoNotAnimate = orderEvent.displayRelevantButDoNotAnimate;

	//if we're going backward, these actually need to be different, because this
	//is the reverse of a DELETE event.
	if (!forwards) {
		//if in reverse, get the previous neighbor
		event = events[orderEvent.previousNeighborID];

		//see if that one is relevant
		relevant = event.relevant;

		//should it be displayed as being relevant even if its not (perfect programmer, non-animated case)
		displayRelevantButDoNotAnimate = event.displayRelevantButDoNotAnimate;
	}

	//IDs of the element in the DOM and the document it's inside of
	var domID = orderEvent.clipNumber + "-" + event.ID;
	var docID = orderEvent.clipNumber + "-" + event.documentID;

	//the item immediately preceeding, as in, where this is going after
	var previousNeighbor = orderEvent.clipNumber + "-" + event.previousNeighborID;

	//check if the current document is active, if not then change it
	if(playback.documentID != docID) {
		//activate the correct document tab
		changeDocument(docID, animate);
	}

	//build the span to be added to the DOM with the id created above
	var span = $("<span/>", {
		id: domID,
		class: 'insert-event'
	}).text(event.eventData);

	//determine where it goes

	//*should* only happen for the first event in a document
	if ($("#" + previousNeighbor).length === 0) {
		//add the first insert event
		span.prependTo("#pre-" + docID);
	}
	//occurs when the previous neighbor is somewhere in the DOM
	else {
		//insert the event into the dom
		span.insertAfter($("#" + previousNeighbor));
	}

	//if the event is relevant in an animated playback or should be highlighted in a
	//non-animated 'perfect programmer' playback, add a class to it
	if(relevant || displayRelevantButDoNotAnimate) {
		span.addClass("display-relevant");
	}

	//if the insert event will get deleted in this playback
	//TODO make this an option in the filter menu??
	if (event.deletedAtTimestamp !== 0) {
		//add a class so that it can be shown in the playback
		span.addClass("deleted-insert");
	}

	//if the current event is relevant, and we want to animate it
	if (relevant && animate) {
		//make the background a subtle green color for 'playback.speed'
		$("#" + domID).css('background', '#cade97');

	//make the new event highlight disappear by the time the next event is played back
		setTimeout(function() { $("#" + domID).css('background', 'transparent'); }, playback.speed);
	}

	//TODO is scrolling as smooth as it should be???
	//scroll to the item
	$("#pre-" + docID).scrollTo(span, {offset: {top: -200}});
}


/*	delete
	When we have a delete event (going forward) or an
	insert event (going backward), we remove the event from the
	screen.
*/
function del(orderEvent, forward, animate) {
	//get the full event
	var event = events[orderEvent.eventID];

	//is it relevant?
	var relevant = orderEvent.relevant;

	//should it be displayed even if it is not relevant (non-animated perfect programmer scenario)
	var displayRelevantButDoNotAnimate = orderEvent.displayRelevantButDoNotAnimate;

	//if we are moving forward when we delete these values are a little different
	if (forward) {
		//get the event's previous neighbor
		event = events[orderEvent.previousNeighborID];

		//is it relevant?
		relevant = event.relevant;

		//should it be displayed even if it isn't relevant
		displayRelevantButDoNotAnimate = event.displayRelevantButDoNotAnimate;
	}

	//generate ids for the html element and doc
	var domID = orderEvent.clipNumber + "-" + event.ID;
	var docID = orderEvent.clipNumber + "-" + event.documentID;

	//check if this event is in the current active document tab, if not then change it
	if(playback.documentID != docID) {
		changeDocument(docID,animate);
	}

	//scroll to the event
	$("#pre-" + docID).scrollTo($("#" + domID), {offset: {top: -200}});

	//if the event that we're deleting is relevant, and we want it animated
	if (relevant && animate) {
		//make the background a subtle red for 'playback.speed' and then remove the dom element
		$("#" + domID).css('background', "#ebaa7c");
		setTimeout(function() { $("#" + domID).remove(); }, (playback.speed));
	}
	//it is not a relevant event
	else {
		//remove the dom element
		$("#" + domID).remove();
	}
}


/*	create document
	When we have a create document event (going forward)
	or a delete document event (going backward), we add
	a new tab to the documents list and a pane for our events
	to be added to.
*/
function createDocument(orderEvent, animate) {
	//get the full event
	var ev = events[playback.orderOfEvents[playback.position].eventID];

	//get a doc id
	var docID = orderEvent.clipNumber + "-" + ev.documentID;

	//the anchor that links to our document. It goes in the tab bar
	var link = $("<a/>", {
		href: '#' + docID,
		id: 'tab-' + docID
	}).text(ev.newName);

	//the actual tab, in which the above anchor goes into
	var item = $("<li/>", {id: "li-" + docID, class: 'clip-' + orderEvent.clipNumber});
	item.append(link);
	item.appendTo($("#documentTabs"));

	//create a div to hold the doc
	var doc = $("<div/>", { id: docID, class: "documentDiv clip-" + orderEvent.clipNumber });
	//create a place for the insert events to go
	var pre = $("<pre/>", { id: "pre-" + docID, class: "document" });

	//add the pre to the doc and the doc to the tabs
	doc.append(pre).appendTo($("#documents"));

	//refresh the tabs
	$("#documents").tabs('refresh');

	//change to the new document
	changeDocument(docID, animate);

	//adjust sizes of things
	setupPlaybackInterface();
}


/*	del document
	Remove a document's tab from the screen.

	This happens on a delete document event (going forward)
	or a create document event (going backward).
*/
function delDocument(orderEvent) {
	//get the full event
	var ev = events[orderEvent.eventID];

	//get the doc id of the document to delete
	var docID = orderEvent.clipNumber + "-" + ev.documentID;

	//remove the doc
	$("#li-" + docID).remove();
	$("#" + docID).remove();
	$("#documents").tabs('refresh');
}

/*	hide clip
	This will hide the document tabs for a clip.

	This is used to transition between two clips.
	If this wasn't here and they used the same document,
	the events would overlap and bad things would happen.
*/
function hideClip(orderEvent) {
	//hide the clip
	$(".clip-" + orderEvent.clipNumber).hide();

	//if we are playing
	if (playback.playing) {
		//pause
		playPause();
	}
}


/*	show clip
	This is used to show a clip when stepping backward.
	It removes the currently shown clip's documents (the
	clip after orderEvent.clipNumber) and shows the previous
	clip.
*/
function showClip(orderEvent) {
	$(".clip-" + orderEvent.clipNumber + 1).remove();
	$(".clip-" + orderEvent.clipNumber).show();
}


/*	Change Documents
	changes the current document with style
	slides current document in and flashes it
*/
function changeDocument(documentID, animate){
	if (animate && playback.playing && playback.documentID !== '') {
		//normal: 200 fastest: 5 slowest: 505
		var speed = playback.speed * 5.5;

		//pause when we slide the next document in
		pause();

		//display a change in the tabs
		$('#' + playback.documentID).hide("highlight",{color:"lightgreen"},1000);
		$('#' + documentID).show("slide", speed, function() {
			setTimeout(speed, play()); //wait for the slide to be done, then play
		});

		//store the current document
		playback.documentID = documentID;
	} else {
		//simulate a lick on the document (non-animated)
		$("#tab-" + documentID).trigger('click');
	}
}
/*
	playback/SETTINGS.js

	Contains everything needed for adjusting your settings uesd in a playback
	(currently only the font size).

	Requirements:
		- playback/main.js
			Holds the main playback object, which is where settings are stored
*/


function setupSettings() {
	$("#settingsMenu").dialog({
		autoOpen: false,
		resizable: true,
		draggable: true,
		height: 220,
		width: 200,
		title: "Settings",

		open: function(event, ui) {
			//set the stored font size in the gui
			$("#curFontSize").val(playback.fontSize);

			$("#curFontSize").keypress(function(event) {
				//if the user presses enter
				if (event.which === 13) {
					//change the font size
					changeFontSize(parseInt($("#curFontSize").val()));
				}
			});
		}
	});
}


/*  toggle settings
	Toggle the settings menu.
*/
function toggleSettings() {
	if ($('#settingsMenu').dialog('isOpen')) {
		$('#settingsMenu').dialog('close');
	} else {
		$('#settingsMenu').dialog('open');
	}
}


/*  dec font size / inc font size
	Decrease or increase the current font size by two
*/
function decFontSize() {
	changeFontSize(parseInt(playback.fontSize) - 2);
}
function incFontSize() {
	changeFontSize(parseInt(playback.fontSize) + 2);
}


/*  change font size
	Change the playback documents' font sizes to the
	specified size, in pixels.
*/
function changeFontSize(size) {
	//store the new font size
	playback.fontSize = size;

	//update the font for all the docs
	$("#fsStyles").html("pre.document { font-size: " + playback.fontSize + "px; }");

	//store the font size in local storage for next time
	localStorage.setItem('size', playback.fontSize);

	//update the current font size
	$("#curFontSize").val(playback.fontSize);
}


/*  get saved settings
	Pull settings saved in local storage and set them.
*/
function getSavedSettings() {
	//if the user has set a speed before, reuse it
	if (localStorage.getItem("speed") !== null) {
		//store the recovered speed
		playback.speed = parseInt(localStorage.getItem("speed"));
	}

	//if the user has set a font size before, reuse it
	if (localStorage.getItem("size") !== null) {
		playback.fontSize = parseInt(localStorage.getItem("size"));
	}
}
/*
	playback/SPEED.js

	Contains everything needed to setup the speed changer and change the speed
	of a playback.

	Requirements:
		- playback/playback.js
			Because it contains the playback object, which holds the speed the
			user wants the playback to move at
*/


/*  document ready
	This sets up the speed slider's functionality and hides it when the document loads.
*/
function setupSpeed() {
	//hide is used so it is not animated
	$("#speedHolder").hide();

	$('#animationsToggle').tooltipster();

	//setup the speed slider, even though it's hidden until you
	//click on the speedometer button.
	$("#speedSlider").slider({
		animate: false,
		min: 0,
		max: 100,
		orientation: "vertical",
		step: 1,
		range: "min",
		value: (101 - (playback.speed / 5)),

		//change the speed of the playback
		slide: function(event, ui) {
			changeSpeed(ui.value);
		},

		// This is required for knowing when to hide the slider.
		// The slider is hidden if the user moves the mouse outside
		// of the speedHolder. If the user clicks and drags the mouse
		// outside of the speedHolder, the speedHolder will not hide
		// until the user releases the mouse.
		stop: function(event, ui) {
			//check if the mouse has unclicked outside of the speedHolder
			if ($("#speedHolder").has($(event.toElement)).length === 0) {
				//create the timeout function to hide the speed slider
				$("#speedHolder").data("leaveTimeout", setTimeout(hideSpeedSlider, 1500));
			}

			//the slider is no longer sliding so it is false
			$("#speedHolder").data("sliding", false);

			//remove focus from the slider's handle so the value cannot be changed
			//by the arrow keys
			$("#speedHolder .ui-slider-handle").blur();
		},

		//sets the sliding data to true so we do not hide the slider when the
		start: function(event, ui) {
			//mouse ventures out of the speedHolder
			$("#speedHolder").data("sliding", true);
		}
	});

	//show the tooltip of the slider's time value when hovering over it
	$("#speedSlider .ui-slider-handle").hover(function(event) {
		showSpeedTooltip();
	});

	//setup our speed slider toggling.
	//If the mouse leaves the speedHolder, set a timeout to hide the speedHolder
	$("#speedHolder").mouseleave(function() {
		if (!$("#speedHolder").data("sliding")) {
			$("#speedHolder").data("leaveTimeout", setTimeout(hideSpeedSlider, 1500));
		}
	});

	//If the mouse enters the speedHolder, remove the timeout to hide the
	//speedHolder
	$("#speedHolder").mouseenter(function() {
		clearTimeout($("#speedHolder").data("leaveTimeout"));
	});

	//Do the same for our speed button now
	//If the mouse leaves the speed button, set a timeout to hide the speedHolder
	$("#speed").mouseleave(function() {
		if ($("#speedHolder").is(":visible")) {
			$("#speedHolder").data("leaveTimeout", setTimeout(hideSpeedSlider, 1500));
		}
	});

	//If the mouse enters the speed button, remove the timeout to hide
	//the speedHolder
	$("#speed").mouseenter(function() {
		clearTimeout($("#speedHolder").data("leaveTimeout"));
	});
}

/*  set up speed slider
	set the size and location of the speed slider
	to become visible.
*/
function setupSpeedSlider() {
	//set the location of the speedholder
	$("#speedHolder").height($(window).height() / 3);
	$("#speedHolder").css("left", ($("#speed").position().left + 9) + "px");
}


/*  toggle speed slider
	Show or hide the speed slider, and position it above
	the speedometer icon.
*/
function toggleSpeedSlider() {
	$("#speedHolder").slideToggle('fast');
}

/*  hide speed slider
	Similar to toggle the speed slider, but it just hides it.
*/
function hideSpeedSlider() {
	$("#speedHolder").slideUp('fast');
}


/*  show speed tooltip
	Show a "super cool", custom tooltip next to the slider's holder widget
	displaying the speed (in ms) of the playback.
*/
function showSpeedTooltip() {
	$("#speed-tool").remove();
	clearTimeout($("#speedSlider").data('timeout'));

	var tooltip = $("<div/>", {
		class: 'custom-tooltip',
		id: 'speed-tool'
	}).text( (playback.speed / 1000) + " sec");

	tooltip.appendTo('body');

	var top = $("#speedHolder").position().top;
	top += $("#speedSlider .ui-slider-handle").position().top;
	top += tooltip.height();

	var left = $("#speedHolder").position().left - tooltip.width();

	tooltip.css({
		left: left,
		top: top,
	});

	$("#speedSlider").data('timeout', setTimeout(function() { $("#speed-tool").remove(); }, 1000));
}

/*  change speed
	Given a precent on the speed slider, ajust the playback's speed,
	in real time.
*/
function changeSpeed(percent) {
	//change the speed (???)
	playback.speed = 505 - (percent * 5);

	//store the new speed in local storage
	localStorage.setItem("speed", playback.speed);

	//if the user selected the max speed
	if (percent === 100) {
		setPlaybackAnimations(false);
	}
	else {
		setPlaybackAnimations(true);
	}

	//if the user changes speed while in a playback
	if (playback.playing) {
		//pause and restart so the new speed takes
		playPause();
		//play at the new speed
		playPause();
	}

	//show the tooltip with the speed
	showSpeedTooltip();
}

function setPlaybackAnimations(animate) {
	animate = !!animate;
	if (animate === playback.animate) {
		return;
	}

	playback.animate = animate;
	if (animate) {
		$("#animationsOffImg").hide();
		$("#animationsOnImg").show();
	} else {
		$("#animationsOnImg").hide();
		$("#animationsOffImg").show();
	}
}

function toggleAnimations() {
	setPlaybackAnimations(!playback.animate);
}
/*
	playback/STORYBOARD.js
	Stuff for playing back a storyboard

	Requirements:
		- playback/playback.js
		- filtering.js
		- login.js
		- global.js
*/

// Storyboard object, holds information for a storyboard...
var storyboard = {
	storyboardID: null, // ID of the storyboard being played
	clips: [],          // list of clips in the storyboard
	//clipNumber: 0,      // the current clip being used
	name: "",
	description: "",
	developerGroupID: null,
};


function commentClick(commentID, idOfClipInDOM) {
	//Get the comment's position in relative events
	//get the comment based on comment id
	var comment = comments[commentID.substr("comment-".length)];
	
	//get the event associated with the comment
	var event = events[comment.eventID];
	
	//get the clip number
	var idSplit = idOfClipInDOM.split("-");
	var clipNum = parseInt(idSplit[1]);
	
	//get the position of the event (where???)
	var pos = findEvent(event.ID, clipNum) + 1;

	//Step to the pos and don't animate things
	step(pos, false);
	console.log(comment);
	
	//highlight the text for this comment
	highlightElements(clipNum + "-" + comment.startHighlightedEventID, clipNum + "-" + comment.endHighlightedEventID);
}

function clipClick(clipID, idOfClipInDOM) {
	//Get the clip/clipNum.
	var idSplit = idOfClipInDOM.split("-");
	var clipNum = idSplit[idSplit.length - 1];
	var clip = storyboard.clips[clipNum];

	//Plus 6 because there are some events in between there (what are the magic 6??).
	var clipEvent = findClipClearEvent(clip.clipNumber) + 6;

	//Step to the position.
	step(clipEvent, false);
}

function storyboardClick() {
	//A storyboard should always be the start.
	step(0, false);
}
$(document).ready(function() {
	setupMovement();
	setupSpeed();
	setupKeyboardShortcuts();
	setupPlayback();
	setupSettings();
});

function startGettingEvents() {

	// Grab all the data from the data.json file.
	events = playbackData.events;
	playback.orderOfEvents = playbackData.orderOfEvents;
	playback.relevantEvents = playbackData.relevantEvents;
	comments = playbackData.comments;
	developers = playbackData.developers;
	developerGroups = playbackData.developerGroups;

	storyboard = playbackData.storyboard;
	showDescription(storyboard);
	showClipInfo(0);

	$("#locationSlider").slider('option', 'max', playback.relevantEvents.length - 1);

	$("#mode").text(storyboard.name);
	setupPlaybackInterface();
	$("#playbackArea").focus();
	showCommentSection();
}

function setupPlaybackRightclickMenu() {}
