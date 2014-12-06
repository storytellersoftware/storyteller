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
