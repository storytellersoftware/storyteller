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


// holds metadata about getting events from the server
var eventGrabber = {
  currentIndex: 0,      // where we're at in grabbing events, aka the number
                        // of events we have at the present

  done: false,          // have we finished grabbing all of the events

  blockSize: 1000,      // number of events to ask the server for with each
                        // request, gets smaller each time the server fails to
                        // handle the request (we receive a 4xx or 5xx error)

  startTime: null,      // a date object of when we started grabbing the events,
                        // used for timing in testing

  endTime: null,        // a date object of when we stopped grabbing the events,
                        // used for timing in testing

  poller: null,         // the id of the interval used for grabbing events

  blocking: false,      // is the grabbing function currently blocking, used to
                        // create a pseudo-semaphore

  clipNumber: 0         // current clip being retreived from the server, only
                        // incremented when we're playing back a storyboard
};

function startGettingEvents() {
  //if this is a request for a storyboard or clip, then there is no playback 
  //session yet on the playback server. create a new (mostly empty) playback session
  if (playback.type == 'storyboard')
  {
    $.getJSON("/playback/new/sessionID/" + getPlaybackSessionId(), function(data) {
      //get the id of the new session
      setPlaybackSessionId(data.sessionID);

      //no need to show the filters or clip creation button
      $("#filters").hide();
      $("#clipcreate").hide();
      $("#export").css({display: "inline"});
      
      //get the events for the storyboard
      getStoryboardEvents();
    });
  }
  else if(playback.type == 'clip') {
    $.getJSON("/playback/new/sessionID/" + getPlaybackSessionId(), function(data) {
      //get the id of the new session
      setPlaybackSessionId(data.sessionID);

      //no need to show the filters or clip creation button
      $("#filters").hide();
      $("#clipcreate").hide();
      
      //get the events for the clip
      getClipEvents();
    });
  }
  //a playback session already exists with an id 
  else if(playback.type == 'filtered' || playback.type == 'selection') {
    //set the text in the top
    setMode("playback");
      
    //get all the nodes and set up the filter menu
    getNodes();
  }
}


/*  grab all events
    Setup things to get all events. Because we needs them. NEEDS THEM
*/
function grabAllEvents() {
  eventGrabber.startTime = Date.now();
  eventGrabber.poller = setInterval(grabEvents, 50);
}


/*  grab events
    Grab the next block of events
*/
function grabEvents() {
  // if we"re already running this, and it hasn't stopped,
  // don"t try again.
  //
  // This is done so that we don't get duplicate events sent
  // from the server
  if (eventGrabber.blocking) return;

  // we're now doing stuff, so don't let this function run in
  // parrallel to itself.
  eventGrabber.blocking = true;

  var startIndex = eventGrabber.currentIndex;
  var endIndex = eventGrabber.currentIndex + eventGrabber.blockSize;

  $.getJSON("/playback/events/sessionID/" + getPlaybackSessionId() + "/from/" + startIndex + "/to/" + endIndex,
    function(data) {
      
      //if the number of events returned is less than the max that means there
      //are no more events and we are done retrieving data from the server.
      if (data.length < eventGrabber.blockSize) {
        //stop the polling 
        clearInterval(eventGrabber.poller)
        delete eventGrabber.poller
        eventGrabber.poller = null;
        
        //set the end time
        eventGrabber.endTime = Date.now()
        
        //indicate we are done
        eventGrabber.done = true
      }

      // increment the current index
      eventGrabber.currentIndex += eventGrabber.blockSize
      

      // add every event we got back from the server to
      // the events object, and to the list of events to be filtered
      $.each(data, function(i, ev) {
        //store the event in a map based on its id
        events[ev.ID] = ev
        
        //add the clip number to the event
        ev.clipNumber = eventGrabber.clipNumber;
        
        //add the event to the collection for playback
        addEventToPlayback(ev);
        
        /*
        eventFilters.eventsToFilter.push({
          eventID: ev.ID,
          clipNumber: eventGrabber.clipNumber,
        })
        */
      })
      
      /*
      // kick off filterEvents if it's the first event
      if (startIndex == 0) {
        eventFilters.startTime = Date.now()
        eventFilters.poller = setInterval(filterEvents, 1)
      }
      */

      // we"re done now, so we don't need to block any more
      eventGrabber.blocking = false

      if (eventGrabber.done) {
        if (playback.type == "storyboard") {// && eventGrabber.clipNumber < storyboard.clips.length) {
          // insert clip clear event
          addEventToPlayback({
            eventID: "CLEAR",
            clipNumber: eventGrabber.clipNumber,
          });
        
          /*
          eventFilters.eventsToFilter.push({
            eventID: "CLEAR",
            clipNumber: eventGrabber.clipNumber,
          })
          */
          
          eventGrabber.clipNumber++
          getNextClip()
        }
        //if this is a regular playback where the user just wants to see the end result
        else if(playback.type == "filtered" && filterMenu.showOnlyEndResult == true) {
          //TODO figure out a way to remove these ui elements or reenable them when someone alters the filters 
          //disable the useless motion controls
          //$("#movement :input").prop('disabled', true);
          //$("#locationHolder :input").prop('disabled', true);
                    
          //we have received all of the events, now display them in one big step
          stepForward(1);
        }
      }
    }
  ).fail(function() {
    // if we couldn't get the events due to some error,
    // try making the block size smaller

    // if the block size can't get any smaller, bad things
    // happened, we let the user know, and ask if they want
    // to try again.
    if (eventGrabber.blockSize == 100) {
      clearTimeout(eventGrabber.poller)
      delete eventGrabber.poller
      eventGrabber.poller = null;

      var tryagain = confirm(
        "The server encountered an extra fatal error, " +
        "something really bad happened. Try again?");

      if (tryagain)
        location.reload()
    }

    // adjust things, try again.
    eventGrabber.blockSize -= 100
    eventGrabber.blocking = false
  })
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


/*  setup playback menu
    Sets up our hijacked right click menu for the playback.
*/
function setupPlaybackRightclickMenu() {
  //TODO make this for selected text playback too
  if (playback.type == "filtered" || playback.type == "selection") {
    createMenu("playbackArea");

    var clipItem = $("<a/>", { href: "#", onclick: "clipCreate()" })
    clipItem.text("Create clip");
    addMenuItem("playbackArea", clipItem)

    var playbackSelection = $("<a/>", {href:"#", onclick: "getEventsWithSelection()"})
    playbackSelection.text("Playback Selection")
    addMenuItem("playbackArea", playbackSelection)

    // We do not currently have a way to display the results for this
    /* var selectionItem = $("<a/>", {href: "#", onclick: "getStoryboardsWithSelection()"})
    selectionItem.text("Find storyboard from selection")
    addMenuItem("playbackArea", selectionItem) */
  }
}

function getStoryboardsWithSelection() {
  $.post("storyboard/selection/sessionID/" + getPlaybackSessionId(),
    {eventIDs: JSON.stringify(getSelectedElements())},
    function(data) {
      // This isn't a thing yet.
      console.log(data)
  })
}