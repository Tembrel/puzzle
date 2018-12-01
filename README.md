# Puzzle solving

This repository contains a Java framework for solving puzzles.

## API usage

TBD

## Dependencies

- Guava
- StreamEx
- JParsec
- ErrorProne (compile-time only)
- Apache Ant (build-time only)


## Building

Clone this repository and navigate to the top-level directory.

Type `ant test` to run the tests (Apache Ant 1.10.x or later).
Use `ant jar` or just `ant` to build a Jar file for the framework
(not including dependencies) in the `build` subdirectory; this
will also run the tests.

The Ant build will download the Apache Ivy jar that manages
dependencies.
By default it will not use an existing Ivy installation
or cache, but this can be changed by overriding properties.
See the [Ivy build properties file](
  ivy/build-ivy.properties
) for details.


## Issues FAQ

TBD