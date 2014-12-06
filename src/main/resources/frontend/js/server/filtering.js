//TODO remove this entire file
/*
		FILTERING.js
		All things related specifically to filtering our events.

		Requirements:
			- something with a filters object
					typically playback/playback-filter-menu.js
*/
/*
// object to hold information about event filtering
var eventFilters = {
	eventsToFilter: [],       // list of event ids that have yet to be filtered
	done: false,              // are we done filtering yet
	startTime: null,          // when we started filtering
	endTime: null,            // when we stopped filtering
	poller: null,             // the interval for the filtering
	irrelevantDifference: 5,  // the amount of time in ms between two events
														// to have us call one irrelevant
}
*/

/*  filter events
		Filter our events?
*/

/*
function filterEvents() {
	// internal - set the relevance of an event, should receive a
	// boolean, as in, is the event relevant or not
	function setRelevance(relevance) {
		currentEvent.relevant = relevance
	}

	// internal - returns the current event object
	function getEvent() {
		return events[currentEvent.eventID]
	}

	function getFilter(attr) {
		try {
			return filters[currentEvent.clipNumber][attr]
		}
		catch(ex) {
			console.log(ex)
		}
	}

	// if we're done filtering, stop doing this
	// TODO: why do we have this?
	if (eventFilters.done) {
		clearInterval(eventFilters.poller);
		return
	}

	// if we're done, stop doing this
	if (eventFilters.eventsToFilter.length == 0 && eventGrabber.done) {
		clearInterval(eventFilters.poller)
		eventFilters.endTime = Date.now()
		delete eventFilters.poller

		eventFilters.done = true;
		return;
	}

	// don't get ahead of the event grabber
	else if (eventFilters.eventsToFilter.length == 0) {
		return;
	}

	// get the current event's id. Shift is like a pop_begin function,
	// it takes returns the first item in a list, and removes that item
	// from the list. It's like dequeue, if lists had queue functions
	//var currentEventID = eventFilters.eventsToFilter.shift()
	var currentEvent = eventFilters.eventsToFilter.shift()


	// initially make everything relevant, and filter out things
	// that aren't relevant later.
	setRelevance(true)

	// if it's a delete, and there time between it and the previous event
	// is less than filters.hideDeletes * 1000 (because ms), IRRELEVANT!
	if (getEvent().type == "DELETE") {

		// find the previous neighbor!
		var previousNeighborIndex = -1
		for (
			var i = playback.orderOfEvents.length - 1;
			i > 0 && playback.orderOfEvents[i].clipNumber == currentEvent.clipNumber;
			i--
		) {
			if (playback.orderOfEvents[i].eventID == getEvent().previousNeighborID) {
				previousNeighborIndex = i
				break
			}
		}

		if (previousNeighborIndex == -1) {
			console.log("Previous neighbor was not found")
			console.log("previousNeighbor: " + getEvent().previousNeighborID)
		}

		currentEvent.previousNeighborIndex = previousNeighborIndex
	}

	// if this is a clear event
	if (getEvent().type == "CLEAR") {
		// nothing!
	}

	// get rid of events not in a relevant document
	else if ('documentID' in getEvent() && ($.inArray(getEvent().documentID, getFilter('documentIDs'))) == -1) {
		return; // This occurs to make sure we don't try to use the event again
	}


		// if it's between hide delete times
	else if(getEvent().type == "DELETE" &&
		'previousNeighborID' in getEvent() && getEvent().previousNeighborID != null &&
		events[getEvent().previousNeighborID] !== undefined &&
		getEvent().timestamp - events[getEvent().previousNeighborID].timestamp <= (getFilter('hideDeletes') * 1000) )
		{
			// find the previousNeighbor

			// remove previousNeighbor (the event being deleted) from
			// the relevantEvents, if it's relavent
			if (playback.orderOfEvents[previousNeighborIndex].relevant)
				playback.relevantEvents.splice(playback.relevantEvents.indexOf(previousNeighborIndex), 1)

			// same for orderOfEvents events
			playback.orderOfEvents.splice(previousNeighborIndex, 1)

			return;
		}


	// if it's by a developer group we don't care about, irrelevant
	else if ('developerGroupID' in getEvent() && ($.inArray(getEvent().developerGroupID, getFilter('developerGroupIDs')) == -1)) {
		setRelevance(false)
	}

	// if it occurred before our initial start time, IRRELEVANT!
	else if (getEvent().timestamp < getFilter('startTime'))
		setRelevance(false)

	// if it occurred after our initial end time, IRRELEVANT!
	else if (getEvent().timestamp > getFilter('endTime'))
		setRelevance(false)

	// if the time between it and previous neighbor is TINY, IRRELEVANT!
	// the assumption is that if there's only a tiny amount of time
	// between two events they probably weren't borne from human hands,
	// and were most likely a copy-paste, or IDE generated snippet.
	else if (
		'previousEventID' in getEvent() && getEvent().previousEventID != null &&
		events[getEvent().previousEventID] !== undefined &&
		getEvent().timestamp - events[getEvent().previousEventID].timestamp < eventFilters.irrelevantDifference)
	{
		setRelevance(false)
	}

	// if we want to show only whole words (filters.gropuingToShow == 1),
	// make any alphanumeric character irrelevant
	else if ('eventData' in getEvent() && getFilter('relevantBlockType') == 'words' && getEvent().eventData.match(/[A-Za-z0-9]/gi))
		setRelevance(false)

	// if we want to show only whole lines (filters.groupingToShow == 2),
	// make any non return characters irrelevant
	// TODO determine if this should be different
	else if ('eventData' in getEvent() && getFilter('relevantBlockType') == 'lines' && getEvent().eventData != '\n')
		setRelevance(false)

	if(playback.type == "selection") {
		if($.inArray(getEvent().ID,selection.eventIDs) == -1)
			setRelevance(false)
	}

	addEventToPlayback(currentEvent)
}


function addEventToPlayback(currentEvent) {
	// is this something that displays comments?
	if (playback.type == "storyboard" || playback.type == "clip") {
		currentEvent = checkEventForComments(currentEvent)
	}

	// add this event to the orderOfEvents array
	playback.orderOfEvents.push(currentEvent)

	// if, after all this filtering, the event is still relevant, add it to
	// our fancy relevantEvents array. relevantEvents is used only with the
	// location slider, because we don't want to let people click on a
	// segment of irrelevant events and screw everything up.
	if (currentEvent.relevant) {
		playback.relevantEvents.push(playback.orderOfEvents.length - 1)
		$("#locationSlider").slider('option', 'max', playback.relevantEvents.length - 1)
	}
}
*/
