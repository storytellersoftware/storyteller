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
	if (window.location.search === '' || window.location.search.length <= 1) {
		// There are no search parameters in the URL.
		return {};
	}
	//throw out the `?`
	var searchString = window.location.search.substring(1);

	return _.transform(searchString.split("&"), function(result, pair) {
		var split = pair.split("=");
		result[split[0]] = split[1];
	});
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
