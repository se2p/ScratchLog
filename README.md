# Scratch 1984

This is the Scratch instrumentation that will log your every move!

## Installation

### Requirements

- Apache Maven
- A MySQL database
- npm
- for data acquisition: instrumented Scratch GUI and Scratch VM

To access the database, you only need to change the connection details in the `application.properties` file.

### Run from within an IDE

Install npm in the `resources/static` folder. The result page of this project uses Google's `Blockly` and
`Scratch Blocks` to display the participant code.
```bash
npm install
```
Since this project is build with the Spring framework, you can run this project from within an IDE. You will still need
to do the necessary configuration described below.

#### Configuration

To get the project up and running, you need to adapt the following configurations to your system:
- Edit the `application.properties` file in the `resources` folder to configure the database connection and mail
  sending.
- Change the URL specified in the `ResourceConfiguration` class in the *addCorsMappings()* method to root context path
  of the application.
- Change the *BASE_URL* string in the `Constants` class to the application URL.
- Change the *GUI_URL* string in the `Constants` class to the URL of the instrumented Scratch GUI.
- Change the *baseURL* property in the instrumented `scratch-vm` project in `logging.js` to `<applicationURL>/store`.
- Change the URL called in the *fetch()* method called in *start()* in the `virtual-machine.js` to to
  `<applicationURL>/store/sb3?id=`.
- Change the `window.location.href` in the *handleFinishExperiment()* method in `menu-bar.jsx` in the instrumented
  `scratch-gui` to the application URL.
- The instrumented `scratch-gui` and `scratch-vm` need to be linked via the `npm link` setting, as described
  [here](https://github.com/LLK/scratch-gui/wiki/Getting-Started).

The `schema.sql` file in `main/resources` contains the necessary database schema. On application startup, a first
administrator is added automatically, if no other administrator could be found in the database. The login credentials
are specified in the `UserInitialization` class.

If you plan to deploy the project under a different context path than the root context path, e.g.
`scratch.fim.uni-passau.de/scratch1984` instead of `scratch.fim.uni-passau.de`, there are a few more things you have to
do:
- The `application.properties` file contains a section where you can configure the context path. Uncomment the two lines
and change the `server.servlet.context-path` accordingly.
- Include your changed context path (e.g. `/scratch1984`) in the url patterns of the ajax requests in
  `participantSuggestions.js` (*getUserSuggestions()*, *getUserDeleteSuggestions()*), `searchSuggestions.js`
  (*getSuggestions()*) and `blockly.js` (*getXML()*) as well as in the `workspace.options.pathToMedia` in`blockly.js`
  (*renderBlockly()*) and in the `location.href` in `searchSuggestions.js` (*setHref()*).

## Build and Deployment

### Deployment
To deploy the project, just execute
```bash
mvn clean compile package
```
This will package the project in a jar ready for execution. In order for the project to work properly, however, it still
requires a running instance of the instrumented `Scratch GUI` linked to the instrumented `Scratch VM` that is accessible
over the `GUI_URL` constant.

### Starting the application

To run the jar, execute
```bash
java -jar scratch1984-0.0.1-SNAPSHOT.jar
```
