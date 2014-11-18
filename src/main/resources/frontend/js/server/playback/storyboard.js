/*  get storyboard events
    Called to kick off getting the events related to a storyboard.

    Where a regular playback gets the events from a node, we need to get
    events only related to the clips inside of the storyboard (because that's
    all a storyboard is, a collection of clips).

    This does the initial setup for a storyboard playback, like adding
    the storyboard's title and description to the comments section,
    and starts the getNextClip() chain.
*/
function getStoryboardEvents() {
  // otherwise we don't have developers and pictures
  getDevelopers();

  // get all clips in storyboard
    $.getJSON('/storyboard/' + storyboard.storyboardID + '/sessionID/' + getPlaybackSessionId(), function(data) {
    storyboard.clips = data.clips;
    storyboard.name = data.name;
    storyboard.description = data.description;
    storyboard.developerGroupID = data.developerGroupID;

    // put the storyboard's title in the title section
    setMode(storyboard.name);
    showCommentSection();

    // add the storyboard's title and description to the comment/notification
    // section.
    showDescription(storyboard);


    // for each clip in the server, add the clip's filters and comments
    // to the filters and comments objects.
    $.each(data.clips, function(i, clip) {
      filters.push(clip.filters);

      addComments(i, clip.comments);

      storyboard.clips[i].clipNumber = i;
    });

    // add a clear event (used to clear a clip and move to the next
    // one) for the first clip. Might not be necessary...
    showClipInfo(0);

    // get the first clip's events
    getNextClip();
  })
}


/*  get next clip
    Grabs the events for the next (or first) clip from the server.

    All it really does is adjust the current filters on the server
    and grabs the events related to the new filters (those filters
    being the clip's).
*/
function getNextClip() {

  // check to make sure we're not going out of bounds
  if (eventGrabber.clipNumber >= storyboard.clips.length) {
    //if we're at the end, remove the last CLEAR event
    if(playback.orderOfEvents[playback.orderOfEvents.length - 1].eventID == "CLEAR") {
      playback.orderOfEvents.splice(playback.orderOfEvents.length - 1, 1);
    }
    /*
    // make sure we don't have an extra CLEAR event at the end of the playback
    if (eventFilters.eventsToFilter[eventFilters.eventsToFilter.length - 1].eventID == "CLEAR")
      eventFilters.eventsToFilter.splice(eventFilters.eventsToFilter.length - 1)
    */
    
    // Make the export button look clickable and function now that our events are all here
    $("#export").removeClass("disabled");
    $("#export").click(exportStoryboard);

    // break out and go crazy!
    return;
  }

  // make sure things are sized correctly
  setupPlaybackInterface();

  // send over the clip's filters and start grabbing the events in the clip 
  $.post('/playback/filter/sessionID/' + getPlaybackSessionId(),
    { filters: JSON.stringify(filters[eventGrabber.clipNumber]) },
    function(data) {

      eventGrabber.currentIndex = 0;
      eventGrabber.done = false;      
      eventGrabber.poller = setInterval(grabEvents, 50);
    }
  );
}


function exportStoryboard() {
  $.post("/playback/export", {
    events: JSON.stringify(events),
    orderOfEvents: JSON.stringify(playback.orderOfEvents),
    relevantEvents: JSON.stringify(playback.relevantEvents),
    comments: JSON.stringify(comments),
    developers: JSON.stringify(developers),
    //TODO is this format correct for developer groups????
    developerGroups: JSON.stringify(developerGroups),
    storyboard: JSON.stringify(storyboard),
  }, function(data) {
    window.location = "/" + data;
  });
}

