# Getting Started developing Storyteller

(AKA: The document that also explains how to Eclipse-ify Storyteller easily)

## Requirements
Storyteller uses [Gradle](http://gradle.org) to manage dependencies and run things. You can either install Gradle, or use the Gradle Wrapper (the `gradlew` and `gradlew.bat` files) to run the tasks.

If you’re going to use gradle itself, make sure you can run `gradle` in your terminal. If you’re going to use the wrapper, try running `./gradlew` on OS X or Linux, or `gradlew.bat` on Windows. You may need to mark the `gradlew` file as executable if it doesn’t initially work.

## Bootstrapping Storyteller
Technically all you need to run the Storyteller Server is Gradle and the source. If you want to actually play around with Storyteller, and watch some text get played back, you also need the [Eclipse Plugin](https://github.com/storytellersoftware/eclipse_plugin).

Once you have the source downloaded, just run `gradle run`, and watch the Storyteller Server startup. You can now run the Eclipse Plugin.

## Using Storyteller and Eclipse
**NOTE** We are unsure if the Eclipse Gradle plugin works with the Gradle Wrapper. If you are relying exclusively on the wrapper proceed at your own risk.

Thankfully, Gradle has pretty good support for Eclipse. The only thing you really need to do (and only if you want to run Gradle tasks inside of Eclipse) is install the [Gradle Eclipse Plugin](http://marketplace.eclipse.org/content/gradle-integration-eclipse-44#.VF0lM1PF9LU), which is as simple as dragging a link on that page into Eclipse.

To “Eclipse-ify” Storyteller, run `gradle eclipse` inside the project’s repository. This will generate all the requisite Eclipse project files for you. 

After doing that, use Eclipse’s import tool to import an existing project, with the path to wherever you’ve downloaded Storyteller.

If you want to be able to run Gradle tasks inside of Eclipse, you need to now convert the storyteller server project to a Gradle Project. To do that, right click on the project, and choose `Configure -> Convert to Gradle Project`.

Once that’s done, if you want to use the Big Green Button to run the Storyteller Server, click it, and choose “Gradle Build”. You’ll be given a text area to put in Gradle commands you want to run, just put down “run”, and run it.