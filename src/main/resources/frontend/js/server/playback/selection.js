

var selection = {
	eventIDs: [],
	filters: {},
}


function getEventsWithSelection() {
  $.post("/playback/selectedText/sessionID/" + getPlaybackSessionId(),
    {eventIDs: JSON.stringify(getSelectedElements())},
    function(data) {
        window.open("/playback.html?selectedTextSessionID=" + data.sessionID)
    })
}

/*
function playPlaybackSession(sID) {
  $.getJSON("/playback/session/" + sID,
    function(data) {
    	selection.eventIDs = data.eventIDs
    	selection.filters = data.filters

    	grabAllEvents()
    })
}
*/