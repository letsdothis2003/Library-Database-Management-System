#!/bin/sh

File Path: mvnw (Root Directory)

Standard Maven Wrapper script for Linux/Docker environments

Find the project root

basedir=$(dirname "$0")

If no Maven installation is found, use the one in the build image

if [ -z "$MAVEN_HOME" ]; then
mvn clean package "$@"
else
"$MAVEN_HOME/bin/mvn" clean package "$@"
fi