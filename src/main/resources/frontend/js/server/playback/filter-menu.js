/*
  playback/FILTER-MENU.js
  Just the filter menu, and stuff to kick off getting events.

  So, also the node selector, because that's kinda filter-y in its
  own way.

  Requirements:
    - login.js
        We need developers and developerGroups objects, which are kept there.
*/

/*
  filterMenu holds data about a node lineage. The client passes in a node id 
  and the server creates data about that node lineage like earliest event,
  latest event, developer groups who contributed to the node lineage, 
  documents and directories, etc. This information is used to display a
  filter menu to the user.
*/
var filterMenu = {};

/*
  this holds all the filters used for playback. There will be only one if
  it is a regular playback but there will be one for each clip in a 
  storyboards playback
*/
var filters = [];

//holds all of the JSON document objects from the server
// TODO is there a good reason for this variable?
var documents = {};

//holds all of the nodes with their names and descriptions (among other things)
var nodes = {};

function setupFilterMenu() {
  
  // the node selection dialog, shows the node tree
  //start comment out here
  $("#nodeSelect").dialog({
    autoOpen: false,
    modal: true,
    draggable: true,
    resizeable: true,
    closeOnEscape: false,
    //height: 400,
    width: 400,
    title: "Node Selection",
    position: "center"
  });
  
  // filters dialog, shows all of our shiny filters
  $("#filterSelect").dialog({
    autoOpen:       false,
    modal:          true,
    draggable:      true,
    resizeable:     false,
    closeOnEscape:  false,
    height:         $(window).height() * .8,
    width:          535,
    minWidth:       535,
    title:          "Filters",
    position:       "center",

    buttons: [{
        text: "Submit",
        click: function() { 
          $(this).dialog('close')
          setupPlaybackInterface()
          submitFilters() 
        }
    }],
  });
}


/*  reopen filter menu
    Regrabs the selected node's initial filters and shows the filter
    menu again, to let you change your filters.

    TODO: check if events get cleared here.
*/
function reopenFilterMenu() {
  $("#filterSelect").dialog('option', 'closeOnEscape', true);
  getNode(filterMenu.nodeID);
}


/*  create node tree
    Creates the most beautiful tree that anybody will ever see

    Puts the nodes in nested lists, and provides a button for getting
    information about the selected node.
*/


/*  get nodes
    Get all nodes in the project, pass that data to createNodeTree
*/
function getNodes() {
  //get all the nodes in a particular session
  $.getJSON("/node/all/sessionID/" + getPlaybackSessionId(), function(data) {
    //empty out the previous node information
    nodes = {};
    var rootNode;
    var openNode;

    $.each(data.nodes, function(i, node) {
      if (node.parentNodeID == "null") {
        rootNode = node.ID;
      }

      //record the one open node
      if (node.type == "OPEN_NODE") {
        openNode = node.ID;
        node.name = node.name + " (Open Node)"
        $("#selectedNodeName").empty();
        $("#selectedNodeName").append("Selected Node: " + node.name);
        $("#selectedNodeDescription").empty();
        $("#selectedNodeDescription").append("Description: " + node.description);
      }

      // add node to nodes
      nodes[node.ID] = {
        ID: node.ID,
        lineageNumber: node.lineageNumber,
        parentNodeID: node.parentNodeID,
        children: [],
        parents: [],
        name: node.name,
        description: node.description,
        type: node.type,
      }
    });

    $.each(nodes, function(i, parent) {
      $.each(nodes, function(j, child) {
        if (parent.ID == child.parentNodeID) {
          parent.children.push(child.ID);
          child.parents.push(parent.ID);
        }
      });
    });

    createNodeTree(nodes, rootNode, openNode);
  })
}

function createNodeTree(nodes, rootNode, selectedNode) {
  /*
  This kind of works for node selection. In the future node selection
  will default to the one open node. Then if the user wants to change
  nodes they will click a button on the filter menu and bring up 
  something like this (or use d3.js like adam v used to have it)
  */
  
  //start comment out here
  var ul = $("<ul/>");

  $.each(nodes, function(i, node) {

    node.li = $('<li/>', {id: node.ID});
    var input = $('<input/>', {
      type: 'radio', 
      name: 'node', 
      class: 'node', 
      value: node.ID, 
      //TODO get the name and description of the node in the UI somehow, right now it is only an id
    });

    if (node.ID == selectedNode)
      input.attr('checked', 'checked');

    node.li.append(input);
    node.li.append(node.name);

    node.ul = $('<ul/>');
    if (node.parentNodeID != 'null') {
      nodes[node.parentNodeID].ul.append(node.li);
    }
  });
  $.each(nodes, function(i, node) { node.li.append(node.ul) });

  ul.append(nodes[rootNode].li);
  $("#nodeSelect").append(ul);

  var button = $("<button/>", {id: "selectNode"}).text("Select Node");
  
  button.click(function(event) {
    event.preventDefault();
    console.log("setting filter's nodeID");
    var choseNode = nodes[$("input[name=node]:checked").val()];
    filters.nodeID = choseNode.ID;
    
    console.log(filters);
    $("#selectedNodeName").empty();
    $("#selectedNodeName").append("Selected Node: " + choseNode.name);
    $("#selectedNodeDescription").empty();
    $("#selectedNodeDescription").append("Description: " + choseNode.description);

    $("#nodeSelect").dialog('close');

    getNode(filters.nodeID);
  })
  button.appendTo($("#nodeSelect"));
  
  //end comment out here
  
  setupPlaybackInterface();

  // go straight to filter dialog
  getNode(selectedNode);
}

/*  get node
    Get the information about one node, and open the filters dialog.
*/
function getNode(nodeID) {
  $.getJSON('/node/' + nodeID + '/sessionID/' + getPlaybackSessionId() + '/getfilterparams', function(data) {
    scratch_json(data);
	
	//store the filter parameters from the server (earliest/latest event, etc.)
    filterMenu = data;

    //store the documents in the lineage
    documents = filterMenu.documentArray;

    //set up the filter gui
    setupFilters(data);

    //if this is not a selected text playback
    if(playback.type != "selection") {
      //show the filter select dialog
      $("#filterSelect").dialog('open');
    }
    else {
      submitFilters();
    }
  });
}

function openNodeSelectDialog() {
  $("#nodeSelect").dialog('open');  
}


/*  setup filters
    Setup the filters in the filters menu.
*/
function setupFilters(json) {
  var startDate = new Date(json.startTime);
  var endDate = new Date(json.endTime);

  // start date/time
  $("#startCalendar").datepicker({
    defaultDate:  startDate,
    maxDate:      endDate,
    minDate:      startDate,

    onSelect:     function(dateText, inst) { updateTimes() }
  });

  $("#endCalendar").datepicker({
    defaultDate:  endDate,
    maxDate:      endDate,
    minDate:      startDate,

    onSelect:     function(dateText, inst) { updateTimes() }
  });

   // time selectors
  $(".time").change(updateTimes);

  setTimes();
  makeDocumentsList();
  makeDevelopersList();

  if (localStorage.getItem('hideDeleteLimit') != null)
    $("#hideDeleteLimit").val(localStorage.getItem('hideDeleteLimit'));
  // else is here because Firefox doesn't respect the value attribute on a
  // number input
  else
    $("#hideDeleteLimit").val(0);
}


/*  submit filters
    Sends a filter object to the server, telling it to set our
    session to the specified filters.
*/
function submitFilters() {
  //scratch_json(filters);

  //setupLogin();

  // adjust date/times
  // set the current starting day
  var startDate = $("#startCalendar").datepicker('getDate');
  startDate.setHours($("#startHour").val());
  startDate.setMinutes($("#startMinute").val());
  startDate.setSeconds($("#startSecond").val());

  // set the current ending day
  var endDate = $("#endCalendar").datepicker('getDate');
  endDate.setHours($("#endHour").val());
  endDate.setMinutes($("#endMinute").val());
  //date picker is at the second granularity (not millisecond)
  //add an extra second to account for events that might happen in the fraction 
  //of a second after the end date picker time
  endDate.setSeconds(parseInt($("#endSecond").val()) + 1);

  filterMenu.startTime = startDate.getTime();
  filterMenu.endTime = endDate.getTime();

  // removes these items from the filters, because we don't want
  // any remnant of what we got from the server from the node's initial filters.
  delete filterMenu.developers;
  delete filterMenu.developerGroups;

  var devGroups = [];
  $("input[type=checkbox].developer:checked").each(function() {
    devGroups.push($(this).val());
  })

  filterMenu['developerGroupIDs'] = devGroups;

  // remove the documents item from the filters because we want it fresh
  delete filterMenu.documentArray;
  
  var docs = [];
  $("input[type=checkbox].document:checked").each(function() {
    docs.push($(this).val());
  });

  //store the ids of the selected documents 
  filterMenu['documentIDs'] = docs;

  filterMenu['hideDeleteLimit'] = $("#hideDeleteLimit").val();
  localStorage.setItem("hideDeleteLimit", filterMenu.hideDeleteLimit);
  
  //get the block type- chars, words, lines, endResult
  filterMenu['relevantBlockType'] = $("input[name=relevantBlockType]:checked").val();
  
  //if the block type is show only the end result
  if(filterMenu['relevantBlockType'] == "endResult") {
    //the block type doesn't really matter- but set it to chars anyway
    filterMenu['relevantBlockType'] = "chars";
    
    //indicate that we want end result only
    filterMenu['showOnlyEndResult'] = true;    
  }
  else {
    //this will not be an end result only
    filterMenu['showOnlyEndResult'] = false;
  }
  
  //get whether the user wants paste origins
  filterMenu['showPasteOrigin'] = $("#showPasteOrigin").is(":checked");

  $.post('/playback/filter/sessionID/' + getPlaybackSessionId(), 
    { 'filters': JSON.stringify(filterMenu) }, 
    function (data) {
      filters[0] = filterMenu;

      clearEverything();
      grabAllEvents();
      $("#playbackArea").focus();
    }
  );
}


/*  make developer list
    Put together the list of developers for the filter menu
*/
function makeDevelopersList() {
  //copy the array of all of the developers into the developers object
  developers = filterMenu.developers;
  
  //get the gravatar urls and add them to the developer objects
  getGravatars();
  
  //old
  //TODO this works for normal playback but not storyboards
  //copy the array of all dev groups to the developerGroups object
  //developerGroups = filterMenu.developerGroups;

  $('#filterDevelopers').empty();

  var devAll = $('<label/>', {class: 'checkbox'});
  devAll.append(
    $('<input/>', {
      type: 'checkbox', 
      id: 'dev-all', 
      checked: 'yes'
    })
    .click(function() { 
      $(".developer").prop('checked', $(this).is(':checked')) 
    })
  );
  
  devAll.append($('<strong/>').text('Select All'));
  devAll.appendTo($("#filterDevelopers"));

  $.each(filterMenu.developerGroups, function (i, devGroup) {
    //scratch_json(doc);
    
    //new
    //add the developer group to the map of all dev groups
    developerGroups[devGroup.developerGroupID] = devGroup;
    
    var devg = $('<label/>', { class: 'checkbox' });
    devg.append($('<input/>', {
      type: 'checkbox',
      checked: 'yes',
      class: 'developer',
      value: devGroup.developerGroupID
    }));

    $.each(devGroup.developers, function(i, devID) {
      var dev = developers[devID];

      var devDisp = $('<span/>', { class: 'tooltip' })
      .text(
        dev.firstName + " " + dev.lastName + 
        (i < devGroup.developers.length - 1 ? ', ' : '')
      );
      
      var devTip = $('<div/>').append(
        $('<div/>', { class: 'devtip' }).append(
          $('<img/>', { 
            src: dev.gravatar + '&s=50'
          })
        ).append($('<code/>').text(dev.email))
      );

      devDisp.attr('title', devTip.html());


      devDisp.appendTo(devg);
    })

    devg.appendTo('#filterDevelopers');
  })

  $('.tooltip').tooltipster();
}


/*  make documents list
    Put together the list of documents for the filter menu
*/
function makeDocumentsList() {
  $('#filterDocuments').empty();

  var docAll = $('<label/>', {class: 'checkbox'});
  docAll.append(
    $('<input/>', {
      type: 'checkbox', 
      id: 'doc-all', 
      checked: 'yes'
    })
    .click(function() { 
      $(".document").prop('checked', $(this).is(':checked')) 
    })
  );
  
  docAll.append($('<strong/>').text('Select All'));
  docAll.appendTo($("#filterDocuments"));

  $.each(filterMenu.documentArray, function (i, doc) {
    //scratch_json(doc);
    var docu = $('<label/>', { class: 'checkbox' });
    docu.append($('<input/>', {
      type: 'checkbox',
      checked: 'yes',
      class: 'document',
      value: doc.documentID
    }));
    docu.append(doc.documentName);
    docu.appendTo('#filterDocuments');
  })
}


/*  setup times
    Set the time fields to be the default start and end times
*/
function setTimes() {
  var startDate = new Date(filterMenu.startTime);
  var endDate = new Date(filterMenu.endTime);

  $("#startHour").val(startDate.getHours());
  $("#startMinute").val(startDate.getMinutes());
  $("#startSecond").val(startDate.getSeconds());

  $("#endHour").val(endDate.getHours());
  $("#endMinute").val(endDate.getMinutes());
  $("#endSecond").val(endDate.getSeconds());
  updateTimes();
}


/*  update times
    Make sure the bounds for the start and end time fields are
    set correctly, and don't overlap.
*/
function updateTimes() {
  // get the potential min and max dates from the filters
  var minDate = new Date(filterMenu.startTime);
  var maxDate = new Date(filterMenu.endTime);

  // set the current starting day
  var startDate = $("#startCalendar").datepicker('getDate');
  startDate.setHours($("#startHour").val());
  startDate.setMinutes($("#startMinute").val());
  startDate.setSeconds($("#startSecond").val());

  // set the current ending day
  var endDate = $("#endCalendar").datepicker('getDate');
  endDate.setHours($("#endHour").val());
  endDate.setMinutes($("#endMinute").val());
  endDate.setSeconds($("#endSecond").val());

  // set the default min and max bounds on our number choosers
  $(".time_hour").attr('min', 0).attr('max', 23);
  $(".time_min").attr('min', 0).attr('max', 59);
  $(".time_sec").attr('min', 0).attr('max', 59);

  // if the current start date is the same as the minimum date
  if (
    startDate.getYear() == minDate.getYear() &&
    startDate.getMonth() == minDate.getMonth() &&
    startDate.getDay() == minDate.getDay()
  ) {
    // make sure the hours can't go below the first hour
    $("#startHour").attr('min', minDate.getHours());

    if ($("#startHour").val() < minDate.getHours()) {
      $("#startHour").val(minDate.getHours());
      startDate.setHours(minDate.getHours());
    }

    // if the hour is as low as possible, make sure the minutes
    // don't go below the first minute
    if ($("#startHour").val() == minDate.getHours()) {
      $("#startMinute").attr('min', minDate.getMinutes());

      if ($("#startMinute").val() < minDate.getMinutes()) {
        $("#startMinute").val(minDate.getMinutes());
        startDate.setMinutes(minDate.getMinutes());
      }

      // if the minutes is as low as possible, make sure the seconds
      // don't go below the first second
      if ($("#startMinute").val() == minDate.getMinutes()) {
        $("#startSecond").attr('min', minDate.getSeconds());

        if ($("#startSecond").val() < minDate.getSeconds()) {
          $("#startSecond").val(minDate.getSeconds());
          startDate.setSeconds(minDate.getSeconds());
        }
      }
    }
  }

  // if the current end date is the same as the minimum date
  if (
    endDate.getYear() == minDate.getYear() &&
    endDate.getMonth() == minDate.getMonth() &&
    endDate.getDay() == minDate.getDay()
  ) {
    // make sure the hours can't go below the first hour
    $("#endHour").attr('min', minDate.getHours());

    if ($("#endHour").val() < minDate.getHours()) {
      $("#endHour").val(minDate.getHours());
      endDate.setHours(minDate.getHours());
    }

    // if the hour is as low as possible, make sure the minutes
    // don't go below the first minute
    if ($("#endHour").val() == minDate.getHours()) {
      $("#endMinute").attr('min', minDate.getMinutes());

      if ($("#endMinute").val() < minDate.getMinutes()) {
        $("#endMinute").val(minDate.getMinutes());
        endDate.setMinutes(minDate.getMinutes());
      }

      // if the minutes is as low as possible, make sure the seconds
      // don't go below the first second
      if ($("#endMinute").val() == minDate.getMinutes()) {
        $("#endSecond").attr('min', minDate.getSeconds());

        if ($("#endSecond").val() < minDate.getSeconds()) {
          $("#endSecond").val(minDate.getSeconds());
          endDate.setSeconds(minDate.getSeconds());
        }
      }
    }
  }

  // if the current start date is also the current end date
  if (
    startDate.getYear() == endDate.getYear() &&
    startDate.getMonth() == endDate.getMonth() &&
    startDate.getDay() == endDate.getDay()
  ) {

    // set mins and maxs for hours
    $("#startHour").attr('max', endDate.getHours());
    $("#endHour").attr('min', startDate.getHours());

    // check if they're in bounds
    if ($("#startHour").val() > endDate.getHours()) {
      $("#startHour").val(endDate.getHours())
      startDate.setHours(endDate.getHours());
    }

    // if starthour == endhour
    if (startDate.getHours() == endDate.getHours()) {
      // set minutes min/max
      $("#startMinute").attr('max', endDate.getMinutes());
      $("#endMinute").attr('min', startDate.getMinutes());

      // check if they're in bounds
      if ($("#startMinute").val() > endDate.getMinutes()) {
        $("#startMinute").val(endDate.getMinutes());
        startDate.setMinutes(endDate.getMinutes());
      }

      // if startmin == endmin
      if (startDate.getMinutes() == endDate.getMinutes()) {
        // set seconds min/max
        $("#startSecond").attr('max', endDate.getSeconds());
        $("#endSecond").attr('min', startDate.getSeconds());

        // check bounds
        if ($("#startSecond").val() > endDate.getSeconds()) {
          $("#startSecond").val(endDate.getSeconds());
          startDate.setSeconds(endDate.getSeconds());
        }
      }
    }
  }

  // if the end date is also the maximum possible date
  if (
    endDate.getYear() == maxDate.getYear() &&
    endDate.getMonth() == maxDate.getMonth() &&
    endDate.getDay() == maxDate.getDay()
  ) {
    // make sure the hour doesn't go over
    $("#endHour").attr('max', maxDate.getHours());
    if ($("#endHour").val() > maxDate.getHours())
      $("#endHour").val(maxDate.getHours());

    // same for minutes
    if ($("#endHour").val() == maxDate.getHours()) {
      $("#endMinute").attr('max', maxDate.getMinutes());
      if ($("#endMinute").val() > maxDate.getMinutes())
        $("#endMinute").val(maxDate.getMinutes());

      // same for seconds
      if ($("#endMinute").val() == maxDate.getMinutes()) {
        $("#endSecond").attr('max', maxDate.getSeconds());
        if ($("#endSecond").val() > maxDate.getSeconds())
          $("#endSecond").val(maxDate.getSeconds());
      }
    }
  }
}