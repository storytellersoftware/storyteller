/*
  playback/SPEED.js

  Contains everything needed to setup the speed changer and change the speed
  of a playback.

  Requirements:
    - playback/playback.js
        Because it contains the playback object, which holds the speed the 
        user wants the playback to move at
*/


/*  document ready
    This sets up the speed slider's functionality and hides it when the document loads.
*/
function setupSpeed() {
  //hide is used so it is not animated
  $("#speedHolder").hide(); 

  $('#animationsToggle').tooltipster();

  //setup the speed slider, even though it's hidden until you
  //click on the speedometer button.
  $("#speedSlider").slider({
    animate: false,
    min: 0,
    max: 100,
    orientation: "vertical",
    step: 1,
    range: "min",
    value: (101 - (playback.speed / 5)),

    //change the speed of the playback
    slide: function(event, ui) {
      changeSpeed(ui.value); 
    },
    
    /*
      This is required for knowing when to hide the slider.
      The slider is hidden if the user moves the mouse outside
      of the speedHolder. If the user clicks and drags the mouse
      outside of the speedHolder, the speedHolder will not hide
      until the user releases the mouse.
    */
    stop: function(event, ui) {
      //check if the mouse has unclicked outside of the speedHolder
      if ($("#speedHolder").has($(event.toElement)).length == 0) {
        //create the timeout function to hide the speed slider
        $("#speedHolder").data("leaveTimeout", setTimeout(hideSpeedSlider, 1500));
      }

      //the slider is no longer sliding so it is false
      $("#speedHolder").data("sliding", false);

      //remove focus from the slider's handle so the value cannot be changed 
      //by the arrow keys
      $("#speedHolder .ui-slider-handle").blur();
    },

    //sets the sliding data to true so we do not hide the slider when the
    start: function(event, ui) {
      //mouse ventures out of the speedHolder
      $("#speedHolder").data("sliding", true);
    }
  });
     
  //show the tooltip of the slider's time value when hovering over it
  $("#speedSlider .ui-slider-handle").hover(function(event) {
    showSpeedTooltip();
  });

  //setup our speed slider toggling.  
  //If the mouse leaves the speedHolder, set a timeout to hide the speedHolder
  $("#speedHolder").mouseleave(function() {
    if (!$("#speedHolder").data("sliding")) {
      $("#speedHolder").data("leaveTimeout", setTimeout(hideSpeedSlider, 1500));
    }
  });

  //If the mouse enters the speedHolder, remove the timeout to hide the 
  //speedHolder
  $("#speedHolder").mouseenter(function() {
    clearTimeout($("#speedHolder").data("leaveTimeout"));
  });

  //Do the same for our speed button now
  //If the mouse leaves the speed button, set a timeout to hide the speedHolder
  $("#speed").mouseleave(function() {
    if ($("#speedHolder").is(":visible")) {
      $("#speedHolder").data("leaveTimeout", setTimeout(hideSpeedSlider, 1500));
    }
  });

  //If the mouse enters the speed button, remove the timeout to hide 
  //the speedHolder
  $("#speed").mouseenter(function() {
    clearTimeout($("#speedHolder").data("leaveTimeout"));
  })
}

/*  set up speed slider
    set the size and location of the speed slider
    to become visible.
*/
function setupSpeedSlider() {
  //set the location of the speedholder
  $("#speedHolder").height($(window).height() / 3);
  $("#speedHolder").css("left", ($("#speed").position().left + 9) + "px");
}


/*  toggle speed slider
    Show or hide the speed slider, and position it above
    the speedometer icon.
*/
function toggleSpeedSlider() {
  $("#speedHolder").slideToggle('fast');
}

/*  hide speed slider
    Similar to toggle the speed slider, but it just hides it.
*/
function hideSpeedSlider() {
  $("#speedHolder").slideUp('fast');
}


/*  show speed tooltip
    Show a "super cool", custom tooltip next to the slider's holder widget
    displaying the speed (in ms) of the playback.
*/
function showSpeedTooltip() {
  $("#speed-tool").remove();
  clearTimeout($("#speedSlider").data('timeout'));

  var tooltip = $("<div/>", {
    class: 'custom-tooltip', 
    id: 'speed-tool'
  }).text( (playback.speed / 1000) + " sec");

  tooltip.appendTo('body');

  var top = $("#speedHolder").position().top + 
            $("#speedSlider .ui-slider-handle").position().top + 
            tooltip.height();
            
  var left = $("#speedHolder").position().left - tooltip.width();

  tooltip.css({
    left: left,
    top: top,
  });

  $("#speedSlider").data('timeout', setTimeout(function() { $("#speed-tool").remove() }, 1000));
}

/*  change speed
    Given a precent on the speed slider, ajust the playback's speed,
    in real time.
*/
function changeSpeed(percent) {
  //change the speed (???)
  playback.speed = 505 - (percent * 5);
  
  //store the new speed in local storage
  localStorage.setItem("speed", playback.speed);

  //if the user selected the max speed
  if (percent === 100) {
    setPlaybackAnimations(false);
  }
  else {
    setPlaybackAnimations(true);
  }
  	
  //if the user changes speed while in a playback 
  if (playback.playing) {
    //pause and restart so the new speed takes
    playPause();
    //play at the new speed
    playPause();
  }

  //show the tooltip with the speed
  showSpeedTooltip();
}

function setPlaybackAnimations(animate) {
  animate = !!animate;
  if (animate === playback.animate) {
    return;
  }

  playback.animate = animate;
  if (animate) {
    $("#animationsOffImg").hide();
    $("#animationsOnImg").show();
  }
  else {
    $("#animationsOnImg").hide();
    $("#animationsOffImg").show();
  }
}

function toggleAnimations() {
  setPlaybackAnimations(!playback.animate);
}
