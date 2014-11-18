/*
    RIGHTCLICK.js
    Controls for right clicking. And making custom menus. Which look nice?

    Basically, abstracting the menu generation away from other things.
    Because who doesn't like abstractions?

    TODO: make better - unabstract this, it's done poorly
*/

// holds menu information
var menu = {}

function setupRightClick() {
  // remove custom menus when someone clicks somewhere
  $(document).bind('click', function(event) { $(".custom-menu").remove() })
}

/*  create menu
    Create a new menu for the passed in element's ID. Yes, ID.

    We hijack the right click on that element, because we want
    custom menus.
*/
function createMenu(elementID) {
  menu[elementID] = []

  $("#" + elementID).bind('contextmenu', function(event) { 
    //event.preventDefault();
    putMenu(elementID, event);
  })
}


/*  put menu
    Adds the menu to the screen at your mouse's position.
*/
function putMenu(elementID, event) {
  $(".custom-menu").remove();

  console.log(event.target)
  if (event.target.nodeName == "TEXTAREA" || event.target.nodeName == "INPUT")
    return

  event.preventDefault()
  var div = $("<div/>", { class: 'custom-menu' })
  $.each(menu[elementID], function(i, item) {
    if (i === "separator")
      div.append("<hr/>")
    else
      div.append(item)
  })

  var top = event.pageY;
  var left = event.pageX;

  div.appendTo("body");

  if (top + div.outerHeight() > $(window).height())
    top = $(window).height() - div.outerHeight();

  if (left + div.outerWidth() > $(window).width())
    left = $(window).width() - div.outerWidth()

  div.css({top: top + "px", left: left + "px"})
}


/*  add menu item
    Add an item to a menu. Items should be jquery objects
*/
function addMenuItem(elementID, item) {
  menu[elementID].push(item)
}


/*  change menu item
    Change a menu item to the passed in `item`.
    Should be a jquery object.
*/
function changeMenuItem(elementID, num, item) {
  menu[elementID][num] = item
}


/*  remove menu item
    Remove an item from a menu.
*/
function removeMenuItem(elementID, num) {
  menu[elementID].splice(num, 1)
}

