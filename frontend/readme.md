# New Browser Stuffs

The new stuff uses SCSS files compiled into CSS using 
[SASS's](http://sass-lang.com) command line tool (you can probably use a 
graphical tool, but I don't know).

Basically, if you're going to be changing any styles, you'll need some sort of
compiler.

To install SASS's CLI tool, you'll need ruby and ruby gems installed. If
you've got those, just run

  gem install sass --no-ri --no-rdoc

You don't need the documentation, it's just annoying to wait that long for
essentially nothing to install.

To have the tool watch the scss and turn it into css when you save an scss
file, navigate to the `BrowserStuff/new` directory, and run

  sass --watch scss:style

If you're going to be changing things, please only play with the 
`scss/_storyteller.scss` file, that way we don't screw up the other bootstrap
stuff.