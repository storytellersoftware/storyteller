/*
  playback/STORYBOARD.js
  Stuff for playing back a storyboard

  Requirements:
    - playback/playback.js
    - filtering.js
    - login.js
    - global.js
*/

// Storyboard object, holds information for a storyboard...
var storyboard = {
  storyboardID: null, // ID of the storyboard being played
  clips: [],          // list of clips in the storyboard
  //clipNumber: 0,      // the current clip being used
  name: "",
  description: "",
  developerGroupID: null,
};


function commentClick(commentID, idOfClipInDOM) {
  //Get the comment's position in relative events
  //get the comment based on comment id
  var comment = comments[commentID.substr("comment-".length)];
  
  //get the event associated with the comment
  var event = events[comment.eventID];
  
  //get the clip number
  var idSplit = idOfClipInDOM.split("-");
  var clipNum = parseInt(idSplit[1]);
  
  //get the position of the event (where???)
  var pos = findEvent(event.ID, clipNum) + 1;

  //Step to the pos and don't animate things
  step(pos, false);
  console.log(comment);
  
  //highlight the text for this comment
  highlightElements(clipNum + "-" + comment.startHighlightedEventID, clipNum + "-" + comment.endHighlightedEventID);
}

function clipClick(clipID, idOfClipInDOM) {
  //Get the clip/clipNum.
  var idSplit = idOfClipInDOM.split("-");
  var clipNum = idSplit[idSplit.length - 1];
  var clip = storyboard.clips[clipNum];

  //Plus 6 because there are some events in between there (what are the magic 6??).
  var clipEvent = findClipClearEvent(clip.clipNumber) + 6;

  //Step to the position.
  step(clipEvent, false);
}

function storyboardClick() {
  //A storyboard should always be the start.
  step(0, false);
}

