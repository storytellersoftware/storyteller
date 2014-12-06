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
