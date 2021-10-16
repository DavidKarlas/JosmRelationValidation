
# JOSM plugin for validating relations

Main montivation for this plugin came from my colleague Aleksandar Matejević who said that http://ra.osmsurround.org/ is awesome but checking each relation individually is PITA, hence I wrote this plugin that checks all relations loaded in JOSM.

## How to install

The Relation Validation plugin can be installed via JOSM plugin manager.
  * Open Preferences -> Plugins
  * Search for the plugin "Relation Validation Plugin" and install it

## How to use

 1) After installing plugin simply run validations.
 1) Any relation that has more than one disconnected graph which is not closed will appear as an error.
 1) Connect disconnected relation, notice there is possiblity of false positives...
 1) If you find any bug, report it on GitHub.

## How to develop

We use [Gradle plugin for developing JOSM plugins](https://github.com/floscher/gradle-josm-plugin) which simplifies things...
  * Install Gradle https://gradle.org/install/
  * Run `gradle w` to create wrapper in repo
  * Run `./gradlew run` to compile and run JOSM which loads plugin

### Debugging

  * Run `./gradlew debug`
  * In VSCode start debugging

What this will do is start JOSM with listening for debugger to connect on port 2018, and VSCode will connect to that port and start debugging.

## License
Since [JOSM](https://github.com/JOSM/josm) itself is GPL 3.0 it makes sense for this plugin to be too.