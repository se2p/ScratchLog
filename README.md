# Scratch 1984

This is the Scratch instrumentation that will log your every move!

## Installation

### Requirements

- Apache Maven (JARHELL!!!!)
- MySQL
- npm (only for developement)

need MySQL database -> define settings in application.properties (user needs delete, insert, update and select rights
on tables)

### Run from within an IDE

install npm in `resources/static` folder
```bash
npm install
```

#### Configuration

edit `application.properties` in the `resources`folder


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