The following are steps to build, install, and launch Graham Poor's Coding exercise for Platform
Science.

Prerequisites: Git and the full Android Development Environment is installed and path are set up.
Installing Git and Android Studio (Easier than installing the Android SDK and Tools separately) 
and possible setting executable paths should be all you need to do.
Note: You may need to replace "adb" with the path to the Android Debug Bridge if it's not in your 
system's PATH.

From the command line:
1) Open and terminal program and navigate to a directory you want the projects root directory to be in.
2) Clone the GitHub repository: git clone https://github.com/grahamp/graham_coding_exercise.git
3) Navigate to the project root directory: cd graham_coding_exercise
4) Build the app using Gradle: ./gradlew assembleDebug
5) Make sure that you have a device or emulator connected to your computer before running the installation command
6) Install the app on your device or emulator: adb install app/build/outputs/apk/debug/app-debug.apk
7) Launch the app using the following command: adb shell am start -n com.grahampoor.ps/.MainActivity

Note: Running the app you will find it takes up to a minute to see
drivers appear in the list on screen, though you should immediately
see a message saying "No Route Available: Processing" 
This is because of the processing required to find the optimum route set in this implementation.
We can discuss ways this issue can be addressed.

There is also a "Show Route" button that opens Google Maps to
the selected destination. This was not in the specification, but
was useful in development to imagine a complete potential user flow.

At this point it would be easiest to open the project in Android Studio, to run unit tests, debug, 
analyse and look at the code and application structure:
1) Click on "Open an existing Android Studio project"
2) Navigate to the cloned repository folder and select the "RoutingApp" directory. 
3) Wait for Android Studio to finish syncing the project with Gradle.


You can run test from the commandline too:
./gradlew test

The following will show other options
./gradlew

