/*
    CLIP-CREATION.js 
    All of the stuff for creating clips.

    Requirements:
      login.js - because we need to be logged in to make clips
*/


// object to hold inforamtion about creating clips, because
// grouping variables together is useful.
var clipCreation = {
  inUse: false,
}

function setupClipCreation() {

  // setup the dialog which asks for a name and description
  $("#clipCreationDialog").dialog({
    autoOpen:false,
    resizable:false,
    width: 500,
    title:"Create a Clip",
    position:$("#playback_clip").position(),
    buttons: [
    {
      text: "Create Clip",
      click: function() { newClip(); }

    }],
  });


  $("#submitComment").click(function(event) { 
    event.preventDefault(); 
    submitComment(clipCreation.selectedElements) 
  })

}

/*  clip create
    Checks if the user is logged in, if they are, it
    opens the clip creation dialog.
*/
function clipCreate() {
/*
  if (!login.loggedIn) {
    $("#loginbox").data("function", "clipCreate")
    $("#loginbox").dialog("open")
    return;
  }
*/

  $("#clipCreationDialog").dialog("open")

  var newComment = $("<a/>", { href: "#", onclick: "commentCreate()" })
  newComment.text("Add a comment");

  changeMenuItem("playbackArea", 0, newComment)

  var finishClip = $("<a/>", { href: "#", onclick: "closeClip()" })
  finishClip.text("Close Clip")

  addMenuItem("playbackArea", finishClip)
}

/*  new clip
    Send the current filters, a name, and a description to the server
    to create a new clip. The server *should* return a clipID, signifying
    that the clip has been created on the server.

    If an error occurs, the user is alerted that the server couldn"t make
    it happen.
*/
function newClip() {
  //$("#clipCreationDialog").dialog("close");
  clipCreation.inUse = true

  var clip = {}

  // hard code, should only be available on regular playbacks
  //clip.filters = JSON.stringify(filters[0]) 
  
  clip.name = $("#clipName").val()
  clip.description = $("#clipDescription").val()
  //clip.developerGroupID = login.group
  clip.sessionID = getPlaybackSessionId()

  $.post("/clip/new/sessionID/" + getPlaybackSessionId(), 
    {clip: JSON.stringify(clip)}, 
    function(data) {
      clipCreation.clip = data.clipID
      setMode("clip creation: " + clip.name)
      $("#clipCreationDialog").dialog("close");
      showGoodNotification("New clip created: " + clip.name, 5000)
    }
  ).fail(function() { alert("the server couldn't process your request"); })
}


/*  comment create
    This allows the user to see the add comment
    sections. It also adds focus to the text area.
*/
function commentCreate() {
  clipCreation.selectedElements = getSelectedElements()
  showCommentSection()
  $("#newComment").show()
}

/*  submit comment
    This will tell the server that a user is now done writing a comment
    and wants to add it to the clip they are creating.
    It will then hide the comment section and clear the text box.
*/
function submitComment(selectedElements) {
  /*
  Expected JSON data (in the "comment" field)
  {
    sessionID:                < a sessionID >,
    commentText:              < text of the comment >,
    eventID:                  < the event the comment opens on >
    startHighlightedEventID:  < the first event to be highlighted : defaulted to null >
    endHighlightedEventID:    < the last event to be highlighted : defaulted to null >
    developerGroupID:         < the ID of the developer group that created the comment >
  }
  */

  var comment = {}
  comment.sessionID = getPlaybackSessionId()
  comment.commentText = $("#commentText").val()

  comment.eventID = playback.orderOfEvents[playback.position - 1].eventID
  if (playback.position == 0)
    comment.eventID = playback.orderOfEvents[0].eventID

  //comment.developerGroupID = login.group
  
  // highlighting
  startElement = selectedElements[0]
  endElement = selectedElements[selectedElements.length - 1]

  if(startElement != null){
    startHightlightEvent = startElement.substr(startElement.indexOf("-") + 1)
    endHighlightedEventID = endElement.substr(endElement.indexOf("-") + 1)
  }
  else {
    startHightlightEvent = 'null'
    endHighlightedEventID = 'null'
  }


  comment.startHighlightedEventID = startHightlightEvent
  comment.endHighlightedEventID = endHighlightedEventID


  $.post("/clip/" + clipCreation.clip + "/sessionID/" + getPlaybackSessionId() + "/comment/new", 
    { comment: JSON.stringify(comment) }, 
    function(data) {
      hideCommentSection()
      $("#commentText").val("");
    }
  )  
}

/* close clip
  This tells the browser that it is done creating a clip
  and it should return to normal playback.
  (As in the right menu changes and setMode is called)
*/
function closeClip() {
  clipCreation.inUse = false
  delete clipCreation.clip
 
  removeMenuItem("playbackArea", 1)
  
  var clipItem = $("<a/>", { href: "#", onclick: "clipCreate()" })
  clipItem.text("Create clip");

  changeMenuItem("playbackArea", 0, clipItem)

  setMode("playback")
}

