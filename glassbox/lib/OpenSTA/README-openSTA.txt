README-OpenSTA	

OpenSTA is a great open source load generation tool and the learning curve isn't too bad.  You can tell it to record, it will then capture your clicks and turn it into a script.  Then you can club those scripts together and run them as a 'test'.  The tests can be scheduled, repeated, simulated with multiple users or called from a command line.

They seem to have a way of getting test results back out, but I haven't looked into that area.

I have version 1.4.3.20 of the commander. 
http://www.opensta.org/


Importing Scripts and Tests:
* The 'Tests' and 'Scripts' folders came from C:\Program Files\OpenSTA\Repository,  put them back there.
* ReStart the commander and it should pick them up.
* Start the OpenSTA name server.


Preparing Scripts and Tests:
* Select each Test,  and go under the menu to compile them. verify there are no errors.
* Start Tomcat or other app on localhost (the scripts assume localhost, but you can change it to another IP in the script easily)
* To create a new Test, go under File->New Test.   Then Drag and Drop Scripts into the Task1 column of the window.   The dragging and dropping is a little clunky.   The other columns are all pretty sensible, try multiple users perhaps,  but don't forget to compile the test before trying to run it.
* To create a new Script,  go under File-> New Script.   Give it a name and double-click it so that you can see it in the Script Modeler.   Hit record and then go clicking around on a website as desired.  Then stop recording.  It's pretty simple.

Running Scripts:
* Make sure the name server is started
* Open the commander
* Double-click on the script in the commander, which pops the Script Modeler window.  Click the 'Replay' button on top.   Note, the button that looks like it could be a red stop-sign, is not.  It's a red record button.  

Running Tests:
* Make sure the name server is started
* Open the commander
* Select a test
* Hit the 'Start Test' arrow at the top of the commander.




