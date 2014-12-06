/*
		LOGIN.js
		Conains all of the stuff required for logging in.
*/

/*
// object that holds information about logins
var login = {
	loggedIn: false,  // has the user logged in yet
	group: null,      // what user group has been selected
}
*/

// objects used everywhere to hold developers and groups
/*
	developerGroups = {
		< developer group id > : {
			developerGroupID: < the same as the key >,
			developers:       [ < array of developer ids > ]
		}, ...
	}
*/
var developerGroups = {};

/*
	developers = {
		< developer id > : {
			ID:         < developer's id >,
			firstName:  < dev's first name >
			lastName:   < dev's last name >
			email:      < dev's email address >,
			gravatar:   < URL to dev's gravatar >
		}, ...
	}
*/
var developers = {};

/*
function setupLoginBox() {
	// prep the modal box for login.
	$("#loginbox").dialog({
		autoOpen:   false,
		title:      "Login",
		draggable:  true,
		resizeable: true,
		closeOnEscape: false,

		// make sure that the user is logged in before letting the
		// modal close
		close: function(event, ui) {
			if (login.loggedIn == false)
				$(this).dialog('open');
		}
	})

	// bind the login link in the nav to open the login box
	$("#login").bind('click', function() { $("#loginbox").dialog('open') })
}
*/

/*  setup login
		Setup the login menu based off of the developers and developerGroups
		objects.
*/
/*
function setupLogin() {
	$.each(developerGroups, function(i, devgroup) {
		var group = $("<label/>", {
			id: 'group-' + devgroup.developerGroupID,
			class: 'groupSelect'
		})

		// add radio button for group
		group.append(
			$("<input/>", {
				type: 'radio',
				name: 'groupSelect',
				value: devgroup.developerGroupID
			})
		)

		$.each(devgroup.developers, function(j, devID) {
			$("<img/>", {
				src: developers[devID].gravatar + '&s=75'
			}).appendTo(group)
		})

		$("#loginbox").append(group).append("<br/>")
	})

	$("<button/>").text("Select Group").bind('click', function(event) {
		event.preventDefault();
		doLogin()
	}).appendTo("#loginbox")
}
*/

/*  do login
		Actually login a user. Checks to make sure a user's been selected.
*/
/*
function doLogin() {
	if ($("input[name=groupSelect]:checked").val() === undefined) {
		alert("That's no moon")
		return;
	}
	login.loggedIn = true
	login.group = $("input[name=groupSelect]:checked").val()

	$("#loginbox").dialog('close')


	// if a function's name is placed in the data, call that function when
	// we close the dialog box. Also, clear the function
	if ($("#loginbox").data('function')) {
		window[$("#loginbox").data('function')]()
		delete $("#loginbox").data('function')
	}
}
*/

/*  get developers
		Retreive all developerGroups from the server and put them in our
		developers and developerGroups objects.
*/
function getDevelopers() {
	$.getJSON('/developer/group/all/sessionID/' + getPlaybackSessionId(), function(data) {
		$.each(data, function(i, devGroup) {
			var group = {
				developers: [],
				developerGroupID: devGroup.ID
			};

			$.each(devGroup.developers, function(j, dev) {
				if (!(dev.ID in developers)) {
					developers[dev.ID] = {
						ID: dev.ID,
						firstName: dev.firstName,
						lastName: dev.lastName,
						email: dev.email
					};
				}

				group.developers.push(dev.ID);
			});

			developerGroups[devGroup.ID] = group;
		});

		// setup developers for login menu
		getGravatars();
		//setupLogin()
	});
}


/*  get gravatars
		add a gravatar to the developer's objects
*/
function getGravatars() {
	$.each(developers, function(i, dev) {
		dev.gravatar = "http://www.gravatar.com/avatar/" + md5(dev.email) + "?d=identicon";
	});
}
