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

Create a new database with a database user to grant the application access and specify the connection details in the
`application.properties` file. Add the necessary tables and constraints using the `schema.sql` file in the
`main/resources` folder.

Install npm in the `resources/static` folder via the following command:
```bash
npm install
```
The result page of this project uses Google's `Blockly` and `Scratch Blocks` to display the participant code. Since this
project is build with the Spring framework, you can run this project from within an IDE. You will still need to do the
necessary configurations described below.

#### Standard configuration

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
- If you only want to use the `application.properties` file, you can comment out the *spring.profiles.active* line.
- Optional: Set the log level to *Warn* or *Error*.

Since the `application.properties` file contains some critical data (e.g. login information for the database) that might
easily be committed by accident, some sections have been commented out. You should put these in a file named
`application-local.properties` in the `resources` folder, uncomment them and adapt them there. This file has been added
to the `.gitignore` file and the *spring.profiles.active* configuration in the `application.properties` file has been
set accordingly for spring to automatically pick up the configurations.

#### Configuring multiple Scratch GUI instances

Since the URL to the instrumented Scratch-GUI instance is saved per experiment, it is possible to use different Scratch
GUIs for individual experiments. To take advantage of this feature, the *app.gui* property can contain a list of
comma-separated URLs **without whitespaces**. The provided URLs will be available as options in a dropdown-menu when
creating a new experiment. The *app.gui.base* property has to be configured accordingly to contain a comma-separated
string of all the unique base URLs of the different Scratch GUI instances (no need to specify the same base URL twice).

#### Deployment under a relative context path

If you plan to deploy the project under a relative context path, e.g.`scratch.fim.uni-passau.de/scratch1984` instead of
`scratch.fim.uni-passau.de`, you need to change the `server.servlet.context-path` in the `application.properties` file
accordingly, e.g. to `/scratch1984` while the *app.url* value is `scratch.fim.uni-passau.de`.

#### Configuring the instrumented Scratch instance

You also have to make some changes to the instrumented Scratch instance:
- Change the *baseUrl* in `logging.js` in the instrumented `scratch-vm` to `<app.url> + <server.servlet.context-path>
  /store`.
- Change the `window.location.href` in the *handleFinishExperiment()* method in `menu-bar.jsx` in the instrumented
  `scratch-gui` to `<app.url> + <server.servlet.context-path>`.
- The instrumented `scratch-gui` and `scratch-vm` need to be linked via the `npm link` setting, as described
  [here](https://github.com/LLK/scratch-gui/wiki/Getting-Started).

On application startup, a first administrator is added automatically, if no other administrator could be found in the
database. The login credentials are specified in the `UserInitialization` class. You should change these credentials
immediately after you have logged in.

#### Configuring SSO Authentication

The application offers functionality to use single sign on through an identity service provider with SAML2. In order to
use this feature, you will have to do some additional configuration in the properties file(s):
- Add `saml2` to the *spring.profiles.active* property.
- Change the *app.saml2.base* string to the base URL of the IdP.
- Change the *saml.username* string to the username pattern with which the username can be extracted from the SAML2
authentication.
- Change the *saml.email* string to the email pattern with which the email can be extracted from the SAML2
authentication.
- Change the *saml.extraction.key* string if needed.
- Change the *saml.extraction.value* string if needed.
- Change the *saml.metadata* string to the path at which the IdP's metadata file is located.
- Change the *saml.idp* string to the name of the IdP.
- Change the *saml.entity* string to the address at which Scratch1984 will be available.
- Change the *saml.certificate* and *saml.key* strings to the paths at which the server certificate and the key file are
available.

Please note that username or email changes from the IDP side are not propagated to the application. This means that if a
user has been authenticated via SSO once and the user has been added to the database, if the user changes their name and
authenticates via SSO again, a new user profile will be created in this case.

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
