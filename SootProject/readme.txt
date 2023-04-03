The code framework is derived from github.com/noidsirius/SootTutorial. I've made some change to this, so now it focuses on generating the callgraph for Android apk and finding network calls.
There's still some bugs wait to fix
to run it: ./gradlew run --args="AndroidCallGraph CHA draw" > youroutput.txt
more details can be found in original project