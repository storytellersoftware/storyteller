/*
  KEYBOARD-SHORTCUTS.js
  Contains all of our keyboard shortcuts
*/

function setupKeyboardShortcuts() {

  // Key Codes:
  var keys = {
    tab:         9,
    enter:      13,
    space:      32,

    arrowLeft:  37,
    arrowRight: 39,
    arrowUp:    38,
    arrowDown:  40,

    comma:     188,   // shift <
    period:    190,   // shift >
  };


  /*
  $(document).keydown(function(event) {
    // show which character is pressed
    console.log(event.which);
  })
  */


  /*  Playback Shortcuts
      
      Spacebar - play or pause
      Right arrow/> - go farward in playback
      Left arrow/< - go backwards in playback
      Up/down arrow - change speed
  */
  $("#playbackArea").keydown(function(event) {
    // Make sure the focus is not on an input element
    if(!$("*:focus").is("textarea, input")) {
      // if we hit the space bar, play/pause
      if (event.which === keys.space) playPause();

      // right arrow  and `>` (well, `.`)
      if (event.which === keys.period || event.which === keys.arrowRight) {
        if (playback.playing) {
          playPause();
        }

        stepForward(1);
      }

      // left arrow and `<` (well, `,`) 
      if (event.which === keys.comma || event.which === keys.arrowLeft) {
        stepBackward(1);
      }

      // up arrow
      if (event.which === keys.arrowUp) {

        // Increment the speed
        $("#speedSlider").slider('option', 
          'value', parseInt($("#speedSlider").slider('value')) + 1);

        // Change the speed
        changeSpeed($("#speedSlider").slider('value'));
        
        // Clear an old timeout to hide the slider
        clearTimeout($("#speedHolder").data("leaveTimeout"));
        
        // Set a new timout
        $("#speedHolder").data(
          "leaveTimeout", setTimeout(hideSpeedSlider, 1500));

        // Show the slider
        if (!$("#speedHolder").is(":visible")) {
          toggleSpeedSlider();
        }
      }

      // down arrow
      if (event.which === keys.arrowDown) {
        // Decrement the speed
        $("#speedSlider").slider(
          'option', 'value', parseInt($("#speedSlider").slider('value')) - 1);

        // Change the speed
        changeSpeed($("#speedSlider").slider('value'));
        
        // Clear an old timeout to hide the slider
        clearTimeout($("#speedHolder").data("leaveTimeout"));
        
        // Set a new timout
        $("#speedHolder").data(
          "leaveTimeout", setTimeout(hideSpeedSlider, 1500));
        
        // Show the slider
        if (!$("#speedHolder").is(":visible")) {
          toggleSpeedSlider();
        }
      }
    }
  });


  // filterSelect Menu
  // Enter - submit the filters
  $("#filterSelect").keydown(function(event) {
    if (event.which === keys.enter) {
      $("#filterSelect").dialog("close");
      setupPlaybackInterface();
      submitFilters();
    }
  });

/*
  // login Menu
  // enter - cause login to occur
  $("#loginbox").keydown(function(event) {
    if (event.which == keys.enter) {
      doLogin();
      }
  });
*/

  // storyboard title
  // tab - open up description box, and switch to it
  $("#storyboardTitle").keydown(function(event) {
    if (event.which === keys.tab && !$("#description").is(':visible')) {
      event.preventDefault();
      toggleDescription();

      $("#storyboardDescription").focus();
    }
  });
  
  // storyboard description
  // tab - close description box, and switch to title
  $("#storyboardDescription").keydown(function(event) {
    if(event.which == keys.tab) {
      event.preventDefault();
      toggleDescription();
      $("#storyboardTitle").focus();
    }
  });
}

