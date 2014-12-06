/*
	playback/SETTINGS.js

	Contains everything needed for adjusting your settings uesd in a playback
	(currently only the font size).

	Requirements:
		- playback/main.js
			Holds the main playback object, which is where settings are stored
*/


function setupSettings() {
	$("#settingsMenu").dialog({
		autoOpen: false,
		resizable: true,
		draggable: true,
		height: 220,
		width: 200,
		title: "Settings",

		open: function(event, ui) {
			//set the stored font size in the gui
			$("#curFontSize").val(playback.fontSize);

			$("#curFontSize").keypress(function(event) {
				//if the user presses enter
				if (event.which === 13) {
					//change the font size
					changeFontSize(parseInt($("#curFontSize").val()));
				}
			});
		}
	});
}


/*  toggle settings
	Toggle the settings menu.
*/
function toggleSettings() {
	if ($('#settingsMenu').dialog('isOpen')) {
		$('#settingsMenu').dialog('close');
	} else {
		$('#settingsMenu').dialog('open');
	}
}


/*  dec font size / inc font size
	Decrease or increase the current font size by two
*/
function decFontSize() {
	changeFontSize(parseInt(playback.fontSize) - 2);
}
function incFontSize() {
	changeFontSize(parseInt(playback.fontSize) + 2);
}


/*  change font size
	Change the playback documents' font sizes to the
	specified size, in pixels.
*/
function changeFontSize(size) {
	//store the new font size
	playback.fontSize = size;

	//update the font for all the docs
	$("#fsStyles").html("pre.document { font-size: " + playback.fontSize + "px; }");

	//store the font size in local storage for next time
	localStorage.setItem('size', playback.fontSize);

	//update the current font size
	$("#curFontSize").val(playback.fontSize);
}


/*  get saved settings
	Pull settings saved in local storage and set them.
*/
function getSavedSettings() {
	//if the user has set a speed before, reuse it
	if (localStorage.getItem("speed") !== null) {
		//store the recovered speed
		playback.speed = parseInt(localStorage.getItem("speed"));
	}

	//if the user has set a font size before, reuse it
	if (localStorage.getItem("size") !== null) {
		playback.fontSize = parseInt(localStorage.getItem("size"));
	}
}
