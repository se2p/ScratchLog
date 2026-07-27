# ScratchLog

## Installation

### Requirements

- JDK 21
  - Note: ScratchLog uses the Lombok compiler plugin to automatically generate getter/setter methods,
    so newer JDK versions might not (yet) work.
- Apache Maven
- A MariaDB or MySQL database
- for data acquisition: instrumented Scratch GUI and Scratch VM
  - https://github.com/se2p/nuzzlebug


### Run from within an IDE

Create a new database with a database user to grant the application access and specify the connection details in the
`application-local.properties` file.
You can then start the application directly via its main method in the `ScratchLogApplication` class.

> 🗒️ Custom settings for local development
>
> Since the `application.properties` file contains some critical data (e.g. login information for the database) that
> might easily be committed by accident, some sections have been commented out. You should put these in a file named
> `application-local.properties` in the `resources` folder, uncomment them and adapt them there. This file has been
> added to the `.gitignore` file and the *spring.profiles.active* configuration in the `application.properties` file
> has been set accordingly for Spring to automatically pick up the configurations.


### Run via Docker Compose

The provided `docker-compose.yml` sets up ScratchLog itself and the required database using [Docker Compose](https://docs.docker.com/compose/):
```bash
docker compose up -d
```
By default, the ScratchLog instance and also the database are only reachable on the local host.
For an actual deployment, you probably want to make ScratchLog available over the network.
For the database it is fine if it remains available locally.

You can find additional configuration options in `src/main/resources/application.properties` or in the ‘Standard Configuration’ section below.
These can be added in the `docker-compose.yml` in the `services>scratchlog>environment` section.
They will become active after a restart (`docker compose restart scratchlog`).
You can find relevant environment variables for the embedding connector in `embedding-connector/README.md`.

To get the automatically generated initial password for the admin user search the log output for something like the following
```console
$ docker compose logs scratchlog
# ... other output ...
********************
User "admin" was added to the database as a first administrator with password ...
"********************"
# ... other output ...
```

> 🗒️
> The Docker Compose at the moment does not deploy an instance of the [Scratch UI](https://github.com/se2p/nuzzlebug).
> It needs to be deployed separately using a regular web server.
> Remember to update the `app.gui` and `app.gui.base` configuration options.


### Build and Deployment

#### Packaging

To deploy the project, execute
```bash
mvn clean package -DskipTests
```
This will package the project in a jar ready for execution in the `target/` directory.
In order for the project to work properly, however, it still requires a running instance of the instrumented
`Scratch GUI` linked to the instrumented `Scratch VM` (https://github.com/se2p/NuzzleBug).

#### Starting the application

To run the jar, copy the `application.properties` from `src/main/resources/` next to the JAR, make the required changes
as outlined below, and execute
```bash
java -jar scratchLog-1.1.0.jar
```

#### Running the tests

We use [Testcontainers](https://testcontainers.com/) to automatically start a container with a database instance during
the tests. Therefore, to run the tests (`mvn test`) Docker or Podman needs to be running on your system.

To use Podman instead of Docker on Linux, enable and start the Podman socket
```bash
systemctl --user enable --now podman.socket
```
and create `~/.testcontainers.properties` in your home directory with the content:
```properties
docker.host=unix:///run/user/1000/podman/podman.sock
docker.client.strategy=org.testcontainers.dockerclient.UnixSocketClientProviderStrategy
```
Replace `1000` with your actual user id (i.e. the output of `id -u`).


### Standard configuration

To get the project up and running, you need to adapt the `application.properties` file in the `resources` folder as
follows:
- Configure the database connection to match your system.
- Change the *app.url* string to the application base URL, e.g. `scratch.fim.uni-passau.de`.
- Change the *app.gui* string to the URL under which the instrumented Scratch GUI is available.
- Set the *app.gui.base* string to the base URL of the Scratch GUI. This might be the same as the *app.gui* property.
  However, if you have deployed the GUI under a relative context path, e.g. `scratch.fim.uni-passau.de/gui`, *app.gui*
  would have to be set to the full path (`scratch.fim.uni-passau.de/gui`) while *app.gui.base* will only be
  `scratch.fim.uni-passau.de`.
- If you only want to use the `application.properties` file, you can comment out the *spring.profiles.active* line.
- If you want to use the application with a mail server, add the `mail` profile to `spring.profiles.active` (takes a
  comma-separated list). Without this setting,
  you will not be able to use the reset password functionality of this application.

On application startup, a first administrator is added automatically if no other administrator could be found in the
database. The login credentials are printed to the log when starting ScratchLog. You should change these credentials
immediately after you have logged in. If you log in too many times with the default administrator password, the account
will be **deactivated**.


#### Configuring multiple Scratch GUI instances

Since the URL to the instrumented Scratch-GUI instance is saved per experiment, it is possible to use different Scratch
GUIs for individual experiments. To take advantage of this feature, the *app.gui* property can contain a list of
comma-separated URLs **without whitespaces**. The provided URLs will be available as options in a dropdown-menu when
creating a new experiment. The *app.gui.base* property has to be configured accordingly to contain a comma-separated
string of all the unique base URLs of the different Scratch GUI instances (no need to specify the same base URL twice).

#### Deployment under a relative context path

If you plan to deploy the project under a relative context path, e.g.`scratch.fim.uni-passau.de/scratchlog` instead of
`scratch.fim.uni-passau.de`, you need to change the `server.servlet.context-path` in the `application.properties` file
accordingly, e.g. to `/scratchlog` while the *app.url* value is `scratch.fim.uni-passau.de`.

#### Configuring the instrumented Scratch instance

To configure the Scratch instance, set the appropriate values in the `.env` file as described in the README file of the
scratch-gui repository before building the instance.

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
- Change the *saml.entity* string to the address at which ScratchLog will be available.
- Change the *saml.certificate* and *saml.key* strings to the paths at which the server certificate and the key file are
available.

Please note that username or email changes from the IDP side are not propagated to the application. This means that if a
user has been authenticated via SSO once and the user has been added to the database, if the user changes their name and
authenticates via SSO again, a new user profile will be created.


## Publications

* Laura Caspari, Luisa Greifenstein, Ute Heuer, and Gordon Fraser. 2023. ScratchLog: Live Learning Analytics for Scratch. In Proceedings of the 2023 Conference on Innovation and Technology in Computer Science Education V. 1 (ITiCSE 2023). Association for Computing Machinery, New York, NY, USA, 403–409. https://doi.org/10.1145/3587102.3588836

* Benedikt Fein and Gordon Fraser. 2026. ScratchLog+: Live Learning Analytics of Learners’ Exercise Progress. 2026 In IEEE/ACM Automated Software Engineering (ASE): Tool and Datasets Track. ACM. https://doi.org/10.1145/3832783.3834618
