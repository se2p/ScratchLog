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
to do the necessary configurations described below.

#### Configuration

To get the project up and running, you need to adapt the `application.properties` file in the `resources` folder as
follows:
- Configure the database connection and mail sending to match your system.
- If you want to use the application without a mail server, set the *app.mail* boolean to false. With this setting,
  you will not be able to use the reset password functionality of this application. Please note that switching between
  mail server options, e.g. first using the application without a mail server and then using one, might cause the
  application to not work properly anymore.
- Change the *app.url* string to the application base URL, e.g. `scratch.fim.uni-passau.de`.
- Change the *app.gui* string to the URL under which the instrumented Scratch GUI is available.
- Set the *app.gui.base* string to the base URL of the Scratch GUI. This might be the same as the *app.gui* property.
  However, if you have deployed the GUI under a relative context path, e.g. `scratch.fim.uni-passau.de/gui`, *app.gui*
  would have to be set to the full path (`scratch.fim.uni-passau.de/gui`) while *app.gui.base* will only be
  `scratch.fim.uni-passau.de`.

If you plan to deploy the project under a relative context path, e.g.`scratch.fim.uni-passau.de/scratch1984` instead of
`scratch.fim.uni-passau.de`, you need to change the `server.servlet.context-path` in the `application.properties` file
accordingly, e.g. to `/scratch1984` while the *app.url* value is `scratch.fim.uni-passau.de`.

You will also have to make some changes to the instrumented Scratch instance:
- Change the *logging._baseUrl* in the *start()* method of `virtual-machine.js` in the instrumented `scratch-vm` to
  `<applicationURL>/store`.
- Change the `window.location.href` in the *handleFinishExperiment()* method in `menu-bar.jsx` in the instrumented
  `scratch-gui` to the application URL.
- The instrumented `scratch-gui` and `scratch-vm` need to be linked via the `npm link` setting, as described
  [here](https://github.com/LLK/scratch-gui/wiki/Getting-Started).

The `schema.sql` file in `main/resources` contains the necessary database schema. On application startup, a first
administrator is added automatically, if no other administrator could be found in the database. The login credentials
are specified in the `UserInitialization` class. You should change these credentials immediately after you have logged
in.

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
