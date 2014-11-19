$(document).ready(function() {
  setupMovement();
  setupSpeed();
  setupKeyboardShortcuts();
  setupPlayback();
  setupSettings();
});

function startGettingEvents() {

  // Grab all the data from the data.json file.
  events = playbackData.events;
  playback.orderOfEvents = playbackData.orderOfEvents;
  playback.relevantEvents = playbackData.relevantEvents;
  comments = playbackData.comments;
  developers = playbackData.developers;
  developerGroups = playbackData.developerGroups;

  storyboard = playbackData.storyboard;
  showDescription(storyboard);
  showClipInfo(0);

  $("#locationSlider").slider('option', 'max', playback.relevantEvents.length - 1);

  $("#mode").text(storyboard.name);
  setupPlaybackInterface();
  $("#playbackArea").focus();
  showCommentSection();
}

function setupPlaybackRightclickMenu(){
  
}

