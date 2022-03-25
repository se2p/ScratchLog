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
- If you want to use the application without a mail server, set the *MAIL_SERVER* boolean in the `Constants` class to
  false. With this setting, you will not be able to use the reset password functionality of this application.
- Change the *BASE_URL* string in the `Constants` class to the application URL.
- Change the *GUI_URL* string in the `Constants` class to the URL of the instrumented Scratch GUI.
- Change the *logging._baseUrl* in *start()* in the `virtual-machine.js` to
  `<applicationURL>/store/sb3?id=`.
- Change the `window.location.href` in the *handleFinishExperiment()* method in `menu-bar.jsx` in the instrumented
  `scratch-gui` to the application URL.
- The instrumented `scratch-gui` and `scratch-vm` need to be linked via the `npm link` setting, as described
  [here](https://github.com/LLK/scratch-gui/wiki/Getting-Started).

The `schema.sql` file in `main/resources` contains the necessary database schema. On application startup, a first
administrator is added automatically, if no other administrator could be found in the database. The login credentials
are specified in the `UserInitialization` class. You should change these credentials immediately after you have logged
in.

If you plan to deploy the project under a different context path than the root context path, e.g.
`scratch.fim.uni-passau.de/scratch1984` instead of `scratch.fim.uni-passau.de`, there are a few more things you have to
do:
- The `application.properties` file contains a section where you can configure the context path. Uncomment the two lines
and change the `server.servlet.context-path` accordingly.
- Change the *CONTEXT_PATH* string in the `Constants` class to the specified context path.

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
