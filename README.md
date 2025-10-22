# The Yoko ORB
Yoko is a CORBA 2.3 compliant ORB implementation to support interprocess communication.
It is an open-source project maintained by IBM, forked from Apache Yoko.

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

## Build Requirements

1. SDKMAN, to manage the toolchain, including installing Java, Gradle

## Steps to build Yoko

1. From the root of the project, run `sdkman env install` to obtain the toolchain. (one-time step)
1. Run `sdk env` to initialise the environment.
1. Run `gradle build` to build and test Yoko.

## Recommended steps to develop Yoko
(We recommend using Linux: YMMV on other platforms.)

1. Fork Yoko on Github
2. Clone your fork into a local git repository.
3. Setup the Python pre-commit framework:
    1) Install Python 3.6 or later
    2) Install pre-commit: `pip install pre-commit`
    3) Run `pre-commit install` to install the pre-commit framework and the hooks.
4. Create a new branch for your work.
5. Make your changes.
6. Run `gradle build` to build and test Yoko.
7. Commit your changes.
8. Push your changes to your fork.
9. Create a pull request to merge your changes into the main repository.

# Testify

Testify is an open-source generic framework used for testing with Junit 5 across multiple threads and forked processes. 

## Docs

The documentation for Testify can be found at https://openliberty.github.io/yoko 
