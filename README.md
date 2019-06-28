# Twitch Alerts

Scala Rest API for managing users, tips, giveaways and polls for a streamer.

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes.

### Prerequisites

- Java
- SBT

### Configuration

Make sure a sqlite database file is available in `db` directory.

Note: You can also edit the database URI by editing `conf/application.conf` file

### Running application in dev mode
```sh
sbt run
```

### Running application in prod mode

There is two ways for running application in prod mode.

The first one, by using this command:
```sh
sbt runProd
```
And the other one, by creating a binary which contains application
```sh
sbt dist
```
Look at the folder `target/universal`, there is a zip file. Unzip it and change directory to the created folder. Then, run application by using this command:
```sh
./bin/twitch-alerts -Dslick.dbs.default.db.url=jdbc:sqlite:/Users/walkoss/Workspace/ESGI/4IBD/Scala/TwitchAlerts/db/db.sqlite
```
Note that we must specify the sqlite database URI. Change it.


### Testing

In order to run tests, run:
```sh
sbt test
```

## Built With

* [Play Framework](https://www.playframework.com/) - Scala web framework
* [Slick](http://slick.lightbend.com/) - Functional Relational Mapping for Scala
* [IntelliJ IDEA](https://www.jetbrains.com/idea/) - IDE

## Author

* **Walid El Bouchikhi**  - [Walkoss](https://github.com/Walkoss)