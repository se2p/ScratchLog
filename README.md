# Scratch 1984

This is the Scratch instrumentation that will log your every move!

## Installation

### Requirements

- Apache Maven (JARHELL!!!!)
- MySQL
- npm (only for developement)
- for data acquisition: instrumented Scratch GUI and Scratch VM

need MySQL database -> define settings in application.properties

### Run from within an IDE

install npm in `resources/static` folder + blockly + scratch-blocks
```bash
npm install
```

#### Configuration

- edit `application.properties` in the `resources`folder
- change `@CrossOrigin(origin = "<url>")` in the `EventRestController` to the application URL
- change `baseURL` property in scratch-vm logging

## Build and Deployment

### Deployment

```bash
mvn clean compile package???
```

### Starting the application

run that jar
```bash
java -jar whatever.jar
```