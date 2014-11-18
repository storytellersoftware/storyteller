var storyboards = {}

function setupStoryboardList() {
  //get any data from the url- ../playback.html?par1=1&par2=2 => {par1: 1, par2:2}
  var searchData = getSearchData();

  //if there's a sessionID in the search data
  if ('sessionID' in searchData) {
    //store the playback session id
    setPlaybackSessionId(searchData.sessionID);
  }

  grabAllStoryboards();
  setMode("Storyboards");  
}


/* grab all storyboards
    This will grab all the storyboards from the
    server and make display them on the DOM.
*/
function grabAllStoryboards() {

  $.getJSON('/storyboard/all/sessionID/' + getPlaybackSessionId(), function(data) {
    storyboards = data

    $.each(storyboards, function(i, storyboard) {
      makeStoryboard(storyboard)
    })
  })
}

/* make storyboard
    This will take a storyboard object and display
    it in a fancy way on the DOM.
*/
function makeStoryboard(storyboard) {
  $("<div/>", {
    id: storyboard.ID,
    class: 'storyboard',
  })
    .html("<h2>" + storyboard.name + "</h2>" + 
          "<pre>" + storyboard.description + "</pre>" +
          "<button onClick='playStoryboard(\"" + storyboard.ID + "\")'>" +
          "<img src='/img/playback/play.svg'></button>" +
          "<button onClick='deleteStoryboard(\"" + storyboard.ID + "\")'>" +
          "<img src='/img/x.svg'></button>")
    .appendTo("#storyboards")
}

/* play storyboard
    This will direct the browser to a storyboard
    playback with the correct storyboard ID.
*/
function playStoryboard(id) {
  window.location = "playback.html?storyboardID=" + id + "&sessionID=" + getPlaybackSessionId();
}

/* delete storyboard
    This will send a delete message to the server to delete the
    indicated storyboard with id of `id`.  This method will then
    remove the storyboard from the list on screen with a
    flawless fade.
*/
function deleteStoryboard(storyboardId) {

  $.get('/storyboard/' + storyboardId + '/delete/sessionID/' + getPlaybackSessionId(), function() {
    $('#'+id).effect("fade", {} , 200, function() {
      $(this).remove()
    })
  })
}

