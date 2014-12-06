/*
	STORYBOARD.js
	Stuff for making storyboards, because storyboards.
*/

// used to hold the scroller's interval
var interval = null;

function setupStoryboardCreate() {
	setMode("storyboard creation");

	// get any data from the url
	// ex: url: ../playback.html?parameter1=1&parameter2=2
	//      => {parameter1: 1, parameter2: 2}
	var searchData = getSearchData();

	//if there's a sessionID in the search data
	if ('sessionID' in searchData) {
		//store the playback session id
		setPlaybackSessionId(searchData.sessionID);
	}

	// get all clips on the server
	getClips();

	// make interface pretty.
	setupStoryboardInterface();

	// setup the bottom timeline to accept clips in a timeline-y fashion
	$("#timeline").sortable({
		scroll: false,
		cursor: "move",
		placeholder: "clipPlaceholder",
		containment: "window",

		// when an item it dragged out of the timeline, delete it!
		stop: function(event, ui) { deleteItem(ui); },
	});


	/*	timeline.sortreceive / timeline.sortout
		Called when an item is placed in the timeline, or when an item
		*not* in the timeline is moved out of the timeline.

		We use this to reset the timeline's width (we don't need it to
		have space for the item we might be dropping in), and we remove
		our nice hover targets from the timeline.
	*/
	$("#timeline").on('sortreceive sortout', function(event, ui) {
		setTimelineWidth(0);
		unhoverizer();
	});


	/*	timeline.sortover
		Called when an item is over (but not dropped into) the timeline.

		We adjust the timeline's width, because our target box is added to
		the timeline, making it bigger, and we add in our nice hover targets
		to permit scrolling.
	*/
	$("#timeline").on('sortover', function(event, ui) {
		setTimelineWidth(0);
		hoverizer();
	});


	/*	clearStoryboard.click
		The button with the red X icon. When it's clicked, we want to remove
		everything from the timeline, by calling empty().
	*/
	$("#clearStoryboard").click(function(event) {
		event.preventDefault();
		$("#timeline").empty();
		setTimelineWidth(0);
	});


	/*	submitStoryboard.click
		The button with the green check icon. When it's clicked, we want to
		submit our storyboard to the server.
	*/
	$("#submitStoryboard").click(function(event) {
		event.preventDefault();
		submitStoryboard();
	});


	/*	storyboardDropdown.click
		The brown down arrow icon. When it's clicked, we want to toggle the
		description box's visibility.
	*/
	$("#storyboardDropdown").click(function(event) {
		event.preventDefault();
		toggleDescription();
	});
}


// when we resize the window, resetup the widths and heights of things.
$(window).resize(function() {
	setupStoryboardInterface();
});


/*	setup storyboard interface
	Called when the window is loaded or resized.

	This adjusts the height of the storyboard creation area to use the whole
	height of the window, less the header.
*/
function setupStoryboardInterface() {
	$("#storyboard").height($(window).height() - 10 - $("header").outerHeight());
}


/*	get clips
	Called when the document is loaded

	Retreive all clips from the server and puts them in the clips div, and
	sets them up to be dragged into the timeline.
*/
function getClips() {

	$.getJSON('/clip/all/sessionID/' + getPlaybackSessionId(), function(data) {
		/*
			`data` *should* be a JSON array of clip objects.

			A clip should look something like:
			{
				ID:                 < clip's id >,
				timestamp:          < when the clip was created >,
				createdUnderNodeID: < node being used when clip was created >,
				playbackNodeID:     < same as createdUnderNodeID >,
				developerGroupID:   < ID of the group that created the clip >,

				name:               < clip's name>,
				description:        < description of clip >,

				comments:           [ < array of clip comments > ],
				filters:            { < filter object > },
			}
		*/

		$.each(data, function(i, clipJSON) {
			// for each clip on the server, create a corresponding div in the
			// clips area. The div should include the name and description
			var div = $("<div/>", {
				class: 'clip',
				id:     clipJSON.ID,
				clipID: clipJSON.ID,
			});
			div.html(
				"<h2>" + clipJSON.name + "</h2>\n" +
				"<p>" + clipJSON.description + "</p>"
			);


			var button = $("<button/>", {}).append(
				$("<img/>", {src: "/img/x.svg"})
			);
			button.click(function(event) {
				event.preventDefault();

				$.get("/clip/delete/" + clipJSON.ID + "/sessionID/" + getPlaybackSessionId(), function(data) {
					$('.clip[clipid=' + clipJSON.ID + ']').fadeRemove();
				});
			}).prependTo(div);

			// add the div to the clips area
			div.appendTo('#clips');

			// setup the clip to be draggable, and dropable in the timeline
			div.draggable({
				revert: "invalid",
				revertDuration: "250ms",
				scroll: false,
				helper: "clone",
				connectToSortable: "#timeline",
				containment: 'window',
				cursor: "move",
				opacity: 0.75,
			});
		});
	});
}


/*	set timeline width
	If an extra amount is passed in, we add that to the specified width

	Because the timeline is inside of a "timeline holder", the timeline is able
	to be as wide as we want and will still scroll. As such, we adjust the
	width of the timeline based on the number of items inside of it.
*/
function setTimelineWidth(extra) {
	var timeline = $("#timeline");
	var bottom = $("#timelineNcommands");

	if (bottom.width() < (timeline.sortable('toArray').length * 220) + extra) {
		timeline.width((timeline.sortable('toArray').length * 220) + extra + "px");
	} else {
		timeline.width(bottom.width() - 10);  // 10 = border
	}
}


/*	submit storyboard
	Called when you click the green check mark button.

	We gather all the information needed for the server to create a  new
	storyboard, and send it in a nice JSON object to the server.
*/
function submitStoryboard() {
/*
	// If the user hasn't logged in yet, we kindly request they login
	if (!login.loggedIn) {
		$("#loginbox").data('function', 'submitStoryboard');
		$("#loginbox").dialog('open');

		// break out. We'll be back because of the loginbox's calling mechanism
		return;
	}
*/

	// if the description area is hidden, and it doesn't have any contents,
	// open it up and prompt the user to add a description.

//  if (!$("#description").is(":visible") && $("#storyboardDescription").val() === "") {
//    toggleDescription();
//    // determine focus based on if the user has set a title already
//    // if they have, choose the description, if they haven't, the title
//    if ($("#storyboardTitle").val() === "") {
//      $("#storyboardTitle").focus();
//    } else {
//      $("#storyboardDescription").focus();
//    }
//    // break out, we don't want to submit a storyboard without a title
//    return;
//  }

	// check that a title has been set
	// now 10% more idiot proof.
	if ($("#storyboardTitle").val().trim() === "") {
		$("#storyboardTitle").focus();
		return;
	}

	/*
		The server is expecting this:

		storyboard = {
			playbackSessionID: < playback session id >,
			developerGroupID: < dev group id >,
			name:             < storyboard name >,
			description:      < storyboard description >,
			clips:            [ < array of clip IDs > ]
		}
	*/

	var clips = [];
	$("#timeline .clip").each(function(i, clip) {
		clips.push($(clip).attr('clipid'));
	});

	var storyboard = {
		//playbackSessionID: sessionID,
		//developerGroupID: login.group,
		name: $("#storyboardTitle").val(),
		description: $("#storyboardDescription").val(),
		clips: clips,
	};

	$.post('/storyboard/new/sessionID/' + getPlaybackSessionId(),
		{ storyboard: JSON.stringify(storyboard) },
		function(data) {
			// do something with that information...
			// open a new window and play the storyboard
			// playStoryboard(data.storyboardID);
			window.location = "playback.html?storyboardID=" + data.storyboardID + "&sessionID=" + getPlaybackSessionId();
		}
	).fail(function() {
		console.log(storyboard);
	});
}


/*	toggle description
	Toggle the description area's visiblity.

	Because we made it look kinda fancy, we need to set some margins
	and heights.
*/
function toggleDescription() {
	// if the description area is visible, hide it
	if ($("#description").is(":visible")) {

		// actually hide the description
		$("#description").hide();

		// reset the height and margin of the left section of timeline commands
		$("#timelineCommands .left").height(50).css({"margin-top": -5});

		// rotate the dropdown icon to be pointed downwards
		$("#storyboardDropdown").css({
			"-moz-transform":     "rotate(0deg)",
			"-webkit-transform":  "rotate(0deg)",
			"transform":          "rotate(0deg)",
		});

		// remove the border from the title input
		$("#storyboardTitle").attr('class', '');
	}

	// if the description area is hidden, show it
	else {

		// show the description
		$("#description").show();

		// give the text area a nice height
		$("#storyboardDescription").height(200);

		// set the height of the left section of timeline commands,
		// making it big enough to hold the title and description boxes
		$("#timelineCommands .left").height(
			$("#description").outerHeight()     +
			$("#storyboardTitle").outerHeight() +
			20                                    // here be magicks
		)
		// style it to 'pop up'
		.css({
			"margin-top": 45 - $("#timelineCommands .left").height(),
		});

		// rotate the dropdown arrow to face up
		$("#storyboardDropdown").css({
			"-moz-transform":     "rotate(180deg)",
			"-webkit-transform":  "rotate(180deg)",
			"transform":          "rotate(180deg)",
		});

		// add an underline to the title input
		$("#storyboardTitle").attr('class', 'border');
	}
}


/*	delete items
	This is called when an item stops being dragged from the timeline.

	We check to see if the item is outside of the timeline, and if so, we
	delete the item.
*/
function deleteItem(ui) {
	// check the item's position against the timeline.
	// The -20 is the padding in the timeline.
	// ui.position holds a position relative to the (0, 0) of
	// the timeline.
	if (ui.position.top < -20 - $(ui.item[0]).height()) {

		// setup the dragged out item for our delete animation
		$(ui.item[0]).css({
			"position": "absolute",

			"top": ui.position.top,
			"left": ui.position.left,
		})
		// bounce a little before deletion
		.effect("bounce", {
			distance: 3,
			times: 1,
			easing: "easeOutSine"
		}, 200)
		// toggle visibility using the 'drop' animation
		.toggle(
			"drop",
			{ easing: "easeOutSine", direction: 'up', distance: '50px' },
			200,

			// function is called after the item is hidden.
			// we want the item removed, because if we don't
			// remove it, it stays in the timeline, just hidden
			function() {
				$(ui.item[0]).remove();
			}
		);

		// reset the timeline's width
		setTimelineWidth(-220);
	}
}


/*	hoverize
	Add nifty hover zones to the timeline holder, which will scroll
	the timeline holder.

	These show up when the user drags a clip from the clip area or the
	timeline over the timeline. They let us move left and right along the
	timeline if it's wider than the window's width.
*/
function hoverizer() {
	// we don't need them if the timeline isn't wider than the
	// area containing it.
	if ($("#timeline").width() < $("#timelineNcommands").width()) {
		return;
	}

	// create the left hover zone
	var leftHover = $("<div/>", {
		class: "hoverizer",
		id: "left-hover",
	});

	// and the right hover zone
	var rightHover = $("<div/>", {
		class: "hoverizer",
		id: "right-hover",
	});

	// add them to the page
	leftHover.appendTo('body');
	rightHover.appendTo('body');

	// setup the left zone to scroll left when you hover on it.
	leftHover.on('mouseenter', function(event) {
		clearInterval(interval);
		interval = setInterval(function() {
			$("#timelineHolder").stop().scrollTo({top: 0, left: '-=10'}, 10);
		}, 10);
	}).on('mouseleave', function(event) {
		// when you stop hovering, stop scrolling.
		clearInterval(interval);
	});


	// same for the right.
	rightHover.on('mouseenter', function(event) {
		clearInterval(interval);
		interval = setInterval(function() {
			$("#timelineHolder").stop().scrollTo({top: 0, left: '+=10'}, 10);
		}, 10);
	}).on('mouseleave', function(event) {
		clearInterval(interval);
	});
}


/*	unhoverizer
	remove the hover zones from the timeline. You don't want them
	when you're not dragging things.
*/
function unhoverizer() {
	clearInterval(interval);
	$(".hoverizer").remove();
}
