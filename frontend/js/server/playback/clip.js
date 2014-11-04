/*
    playback/CLIPS.js
    Contains things specific to clip playbacks

    Requirements:
      - playback/main.js
          Contains grabEvents

      - filtering.js
          Contains filters array

      - global.js
          Contains setMode

      - comments.js
          contains addComments and ShowCommentSection
*/

var clip = {
  clipID: null,
}

/* get clip events
  This will grab all of the events in a clip from
  the server and set up the developer information.
*/
function getClipEvents() {
  getDevelopers()


  $.getJSON('/clip/' + clip.clipID + '/sessionID/' + getPlaybackSessionId(), 
  function(data) {
    filters[0] = data.filters
    addComments(0, data.comments)
    showCommentSection()

    setMode(data.name)

    showDescription(data)

    $.post('/filter/sessionID/' + getPlaybackSessionId(), 
      { filters: JSON.stringify(filters[0]) }, function() {

        setupPlaybackInterface()
        
        eventGrabber.currentIndex = 0
        eventGrabber.done = false

        eventGrabber.poller = setInterval(grabEvents, 50)
      } 
    )
  })
}