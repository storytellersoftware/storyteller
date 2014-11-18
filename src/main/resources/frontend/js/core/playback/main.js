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
  relevantEvents: [], // position of relevant events in orderOfEvents (used for playback slider) //all relevant event IDs, in order of occurrence
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
    $(this).data("timeout", setInterval("stepBackward(1)", playback.speed));
  }).bind("mouseup mouseleave", function() {
    //stops the above interval
    clearInterval($(this).data("timeout"));
  })

  //same as the back seek button, but for the forward seek button
  $("#stepForward").mousedown(function() {
    if (playback.playing) playPause();

    clearInterval($(this).data("timeout"));
    clearInterval($("#stepBackward").data("timeout"));

    //causes initial step if just one click
    stepForward(1);

    //only called if held down, attaches an interval id to the button,
    //which calls stepForward every `playback.speed` ms
    $(this).data("timeout", setInterval("stepForward(1)", playback.speed));
  }).bind("mouseup mouseleave", function() {
    //stops the above interval
    clearInterval($(this).data("timeout"));
  })


  //setup the tab area for playback documents
  $("#documents").tabs({
    collapsible:  false,  //can we collapse the tabs? NO
    hide:         false,  //is this hidden? NO
    active:       1,      //which tab are we starting with? 1
    activate:     function(event, ui) {
                    if(playback.playing && playback.documentID != '') 
                      pause();
                    playback.documentID = ui.newPanel[0].id;                  
                  } //when tabs are clicked
  })

  //get any data from the url- ../playback.html?par1=1&par2=2 => {par1: 1, par2:2}
  var searchData = getSearchData();

  //if there's a storyboardID param, we're playing back a storyboard
  if ('storyboardID' in searchData) {
    //set the playback type
    playback.type = 'storyboard';
    
    //store the id of the storyboard to playback
    storyboard.storyboardID = searchData.storyboardID;

    //if there is a session id in the url    
    if('sessionID' in searchData) {
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
  else if('selectedTextSessionID' in searchData) {
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
  else if('sessionID' in searchData){
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
  events["CLEAR"] = {
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
})


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
  var lhw = $(window).width() - 
            $("#movement").outerWidth() - 
            $("#etcCommands").outerWidth() - 70;

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
  clearInterval($("#stepForward").data("timeout"))
  clearInterval($("#stepBackward").data("timeout"))
  
  //if we are paused
  if(!playback.playing)
  {
    //indicate we are playing 
    playback.playing = true;
    
    //set the time to automatically play at speed slider speed
    playback.player = setInterval("stepForward(1)", playback.speed)
 
    //toggle the buttons
    $("#pauseIcon").show()
    $("#playIcon").hide()
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
  if(playback.playing)
  {
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
  events = {}
  
  //clear out the event grabber
  eventGrabber.done = false
  eventGrabber.currentIndex = 0
  
  //clear out the event ids and relevant event positions
  playback.orderOfEvents = []
  playback.relevantEvents = []

  //clear out the playback location slider
  $("#locationSlider").slider("option", "value", 0)
  $("#locationSlider").slider("option", "max", 0)

  //clear out the documents 
  $("#documents").html(
    "<ul id='documentTabs'></ul>\n" +
    "<div id='developerPictures'></div>"
  )

  //clear out the documents tabs
  $("#documentTabs").tabs({
    collapsible: false,
    hide: false,
    active: 1,
  })
}

/*  change dev group
    Change the currently displayed developer group
*/
function changeDevGroup(currentDevGroup) {
  //if the passed in developer group is NOT the same as the current dev group 
  if($("#developerPictures").data("group") === undefined || 
     $("#developerPictures").data("group") != currentDevGroup ) {
    
    //search the dev groups for the passed in dev group
    //old
    //for(var i = 0; i < developerGroups.length; i++) {
    //new
    //go through each of the developer groups
    $.each(developerGroups, function(devGroupId, devGroup) {
      //old
      //if the dev group was found
      //if(currentDevGroup == developerGroups[i].developerGroupID) {
      //new
      //if we have found the current dev group 
      if(currentDevGroup == devGroupId) {
      
        //store the dev group id
        $("#developerPictures").data("group", currentDevGroup);

        //clear out the div
        $("#developerPictures").html("");
        
        //old
        //for each developer in the dev group add the dev info
        //$.each(developerGroups[i].developers, function(i, devID) {
        //new
        //for each of the developers in the dev group
        $.each(devGroup.developers, function(i, devID) {
            //create a picture of the dev
            $("<img/>", {
            class: "devPicture",
            src: developers[devID].gravatar,
            title: developers[devID].firstName + " " + developers[devID].lastName + " (" + developers[devID].email + ")",
          }).appendTo("#developerPictures")
        })      
      } 
    })
  }
}

//MM- do we use this anymore????
/*  find event
    Finds the relative event number of a passed in eventID and it's clipNumber
*/
function findEvent(eventID, clipNumber) {
  //index of event in playback.orderOfEvents 
  var index = 0

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
  for(var i = 0; i < playback.relevantEvents.length; i++) {
    if(events[playback.orderOfEvents[playback.relevantEvents[i]].eventID].type == "CLEAR") {
      currentClipNumber++;
    }

    if(currentClipNumber == clipNumber) {
      return playback.relevantEvents[i];
    }
  }
}

