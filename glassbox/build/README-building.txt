Building Glassbox requires the following steps:
1. Add external JARs
2. Setup both Java5 and Java1.4
3. Configure Ant
4. Build with Ant
5. (optional) Build Inside Eclipse (without ant)



1. Add External JARs:

Glassbox requires the freely downloadable but not open source Java mail and activation jars. These 
are licensed for distribution with a program, but cannot be redistributed separately.
To build Glassbox, please download the following jar files:

mailapi.jar from http://java.sun.com/products/javamail/downloads/index.html
activation.jar from http://java.sun.com/products/javabeans/jaf/downloads/index.html

Then copy them both to the webClient/web/WEB-INF/lib folder. The appropriate jars should already be 
on the Eclipse classpath of the agent and webClient projects. 


2. Setup both Java5 and Java 1.4

Building Glassbox with Ant requires both a 1.4 and 1.5 JVM, which you can download form java.sun.com  

   a. Set Java5 to be your base JRE in Eclipse by going into Window|Preferences|Installed JREs and making sure to add
      and select the correct Java installation.  
      
      For example: I added the following JRE is C:\Program Files\Java\jdk1.5.0_06 and made sure it was checked.

   b. Specify the path to your 1.4 VM by adding a line to your build/YOUR_USERNAME.build.properties file that
   	  points to your java.exe in your java1.4 installation.  
   	  
   	  For example:  If my username was pickering, I would add the following line into pickering.build.properties 
   	  in the build directory of the build project.
   	  
   	  java14.vm=C:/Program Files/Java/jdk1.4.2.11/bin/java.exe


3. Configure Ant:

Glassbox requires you to add junit.jar for JUnit 3.8.1 to the ant classpath. You can do this by 
adding the jar to your ant/lib folder. For building with ant inside Eclipse you can add the junit.jar 
to the jars included in window|preferences|Ant|Runtime|Global Entries.


4. Build with Ant

Build the default target (core) in the build.xml file located in the Glassbox build project. E.g., 
with just an "ant" command. If you want to build all the release artifacts, you should build the 
release target, e.g., with "ant release". 

Go into the build project, right click on build.xml.  Select "Run As" and "2. Ant Build...".  From within
the Eclipse box, select "rebuild core" and click OK.

If you have problems, I would suggest you go into Project|Clean and select clean all projects.  Then build
your "rebuild core" target again.


5. (Optional) Build Inside Eclipse
To compile Glassbox inside Eclipse you need Eclipse 3.x with a recent developer snapshot build of AJDT, 
the AspectJ plugin for Eclipse. Note that AJDT production releases based on AspectJ 1.5.2 and earlier have 
bugs that cause them to fail with the Glassbox code base. Please use a build that from September 2006 
or later.  See www.eclipse.org/ajdt/downloads/ for instructions on downloading AJDT.

All projects but ltw13 should be compiled in Eclipse with a Java 1.5 VM. Glassbox is 
backwards-compatible with Java 1.4 but some parts require a 1.5 VM's libraries and there are a few 
source code dependencies on 1.5 VM's.

To compile ltw13 inside of Eclipse you need to configure a Java 1.4 VM or JRE and point the project 
at that library as in step 2 above. If you only want to compile inside Eclipse and don't care about
building with Ant, you can also simply close the ltw13 project and develop with a Java 5 VM.

You can build in Eclipse either incrementally or by doing full builds with clean and rebuild. The 
AJDT plugin occasionally has errors on incremental compilation, so cleaning and rebuilding is a good 
strategy if you see an unexpected error.

Please visit the Glassbox Forums at www.glassbox.com/forum/forum/listforums if you get stuck, we can
help.